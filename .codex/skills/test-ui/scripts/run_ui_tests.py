#!/usr/bin/env python3
"""Run the project's Markdown-defined console UI test plan."""

from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path


FENCE = chr(96) * 3
ANSI_ESCAPE = re.compile(r"\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])")
CASE_HEADING = re.compile(
    r"^##\s+Test case(?:\s+\d+)?\s*:\s*(.+?)\s*$", re.IGNORECASE
)
AIM_LINE = re.compile(r"^\s*-\s*Aim:\s*(.+?)\s*$", re.IGNORECASE)
SECTION_HEADING = re.compile(
    r"^###\s+(Inputs?|Expected output)\s*$", re.IGNORECASE
)


@dataclass(frozen=True)
class TestCase:
    """One UI test case parsed from the project test plan."""

    name: str
    aim: str
    inputs: str
    expected: str


@dataclass(frozen=True)
class RunResult:
    """Captured result of one program session."""

    output: str
    returncode: int | None
    error: str | None = None


def extract_fenced_block(lines: list[str], start: int, label: str) -> str:
    """Extract the first Markdown text fence following a section heading."""
    index = start + 1
    while index < len(lines) and not lines[index].strip().startswith(FENCE):
        index += 1
    if index == len(lines):
        raise ValueError(f"{label} section is missing a fenced code block")

    index += 1
    content: list[str] = []
    while index < len(lines) and not lines[index].strip().startswith(FENCE):
        content.append(lines[index])
        index += 1
    if index == len(lines):
        raise ValueError(f"{label} fenced code block is not closed")
    return "\n".join(content).strip("\n")


def parse_plan(plan_path: Path) -> list[TestCase]:
    """Parse and validate all test cases in a Markdown plan."""
    lines = plan_path.read_text(encoding="utf-8").splitlines()
    headings = [
        (index, match.group(1))
        for index, line in enumerate(lines)
        if (match := CASE_HEADING.match(line))
    ]
    if not headings:
        raise ValueError(f"No test cases found in {plan_path}")

    cases: list[TestCase] = []
    for heading_index, (start, name) in enumerate(headings):
        end = (
            headings[heading_index + 1][0]
            if heading_index + 1 < len(headings)
            else len(lines)
        )
        section = lines[start + 1 : end]

        aim = next(
            (match.group(1) for line in section if (match := AIM_LINE.match(line))),
            None,
        )
        if not aim:
            raise ValueError(f"Test case '{name}' is missing an Aim line")

        section_positions = {
            match.group(1).lower(): index
            for index, line in enumerate(section)
            if (match := SECTION_HEADING.match(line))
        }
        if "inputs" not in section_positions or "expected output" not in section_positions:
            raise ValueError(
                f"Test case '{name}' must contain Inputs and Expected output sections"
            )

        inputs = extract_fenced_block(
            section, section_positions["inputs"], "Inputs"
        )
        expected = extract_fenced_block(
            section, section_positions["expected output"], "Expected output"
        )
        input_lines = [line for line in inputs.splitlines() if line.strip()]
        if not input_lines or input_lines[-1].strip() != "bye":
            raise ValueError(
                f"Test case '{name}' must end its Inputs block with 'bye'"
            )

        cases.append(TestCase(name=name, aim=aim, inputs=inputs, expected=expected))
    return cases


def strip_ansi(output: str) -> str:
    """Remove terminal color/control sequences from captured output."""
    return ANSI_ESCAPE.sub("", output).replace("\r", "")


def is_banner_or_separator(line: str) -> bool:
    """Identify fixed console decoration that is not part of an assertion."""
    stripped = line.strip()
    return (
        not stripped
        or set(stripped) == {"─"}
        or stripped.startswith(("/\\", "\\ \\", "\\_\\", "\\/_/"))
        or stripped in {
            "Hello! I'm Megia.",
            "What can I do for you?",
            "Bye. Hope to see you again soon.",
        }
    )


def normalized_output(output: str) -> str:
    """Return meaningful output lines used for expected-output comparison."""
    meaningful: list[str] = []
    for raw_line in strip_ansi(output).splitlines():
        line = raw_line[2:] if raw_line.startswith("> ") else raw_line
        if is_banner_or_separator(line):
            continue
        meaningful.append(line)
    return "\n".join(meaningful).strip()


def display_output(output: str) -> str:
    """Return readable captured console output for the session record."""
    return strip_ansi(output).strip() or "(no console output)"


def compile_project(project_root: Path, build_dir: Path) -> None:
    """Compile all project Java sources using Java 25."""
    source_root = project_root / "src" / "main" / "java"
    sources = sorted(source_root.rglob("*.java"))
    if not sources:
        raise RuntimeError(f"No Java sources found under {source_root}")

    result = subprocess.run(
        [
            "javac",
            "--release",
            "25",
            "-d",
            str(build_dir),
            *(str(path) for path in sources),
        ],
        cwd=project_root,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    if result.returncode != 0:
        raise RuntimeError("Java 25 compilation failed:\n" + result.stdout)


def run_case(
    project_root: Path, build_dir: Path, case: TestCase, timeout: float
) -> RunResult:
    """Run one test case in a fresh Java process and return its output."""
    resources = project_root / "src" / "main" / "resources"
    classpath = os.pathsep.join((str(build_dir), str(resources)))
    try:
        result = subprocess.run(
            ["java", "-cp", classpath, "Megia"],
            cwd=project_root,
            input=case.inputs.rstrip("\n") + "\n",
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            timeout=timeout,
        )
    except subprocess.TimeoutExpired as error:
        output = error.stdout or ""
        if isinstance(output, bytes):
            output = output.decode(errors="replace")
        return RunResult(
            output=output,
            returncode=None,
            error=f"process timed out after {timeout:g} seconds",
        )
    except OSError as error:
        return RunResult(output="", returncode=None, error=str(error))

    return RunResult(
        output=result.stdout,
        returncode=result.returncode,
        error=(
            f"process exited with status {result.returncode}"
            if result.returncode != 0
            else None
        ),
    )


def print_case_record(case: TestCase, actual: str) -> None:
    """Print the input and captured output for one test case."""
    print(f"Console input ({case.name}):")
    print(case.inputs)
    print("Console output:")
    print(display_output(actual))


def main() -> int:
    """Run the plan and stop at the first failed test case."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--plan", type=Path, default=Path("test/ui-test-plan.md"))
    parser.add_argument("--project-root", type=Path, default=Path.cwd())
    parser.add_argument("--timeout", type=float, default=10.0)
    args = parser.parse_args()

    project_root = args.project_root.resolve()
    plan_path = args.plan if args.plan.is_absolute() else project_root / args.plan
    try:
        cases = parse_plan(plan_path)
        with tempfile.TemporaryDirectory(prefix="test-ui-build-") as build_path:
            build_dir = Path(build_path)
            compile_project(project_root, build_dir)

            print(f"UI test session: {plan_path}")
            for number, case in enumerate(cases, start=1):
                run_result = run_case(project_root, build_dir, case, args.timeout)
                actual = run_result.output
                actual_normalized = normalized_output(actual)
                expected_normalized = normalized_output(case.expected)

                if run_result.error or actual_normalized != expected_normalized:
                    print(f"\nFAIL {number}: {case.name}")
                    print(f"Aim: {case.aim}")
                    print_case_record(case, actual)
                    if run_result.error:
                        print(f"Process error: {run_result.error}")
                    print("Actual output (normalized):")
                    print(actual_normalized or "(empty)")
                    print("Expected output:")
                    print(expected_normalized or "(empty)")
                    print("\nStopping the UI test session after the first failure.")
                    return 1

                print(f"\nPASS {number}: {case.name}")
                print(f"Aim: {case.aim}")
                print_case_record(case, actual)

            print(f"\nAll {len(cases)} UI test case(s) passed.")
            return 0
    except (OSError, ValueError, RuntimeError) as error:
        print(f"UI test session could not run: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
