---
name: test-ui
description: Run this project's console UI tests from test/ui-test-plan.md by executing each listed command sequence, comparing the resulting output with the expected output, recording the console session, and stopping immediately on the first failure. Use when validating interactive Java UI behavior or updating the project's UI test cases.
---

# Test UI

Run the project's scripted console UI tests and report the complete session.

## Workflow

1. Read `test/ui-test-plan.md`. Treat each `## Test case` section as an ordered test case.
2. Confirm that every case has an aim, an `Inputs` fenced block, and an `Expected output` fenced block. Require `bye` as the final non-empty input line so the Java program exits cleanly.
3. Run the bundled runner from the project root:

   ```bash
   python3 .codex/skills/test-ui/scripts/run_ui_tests.py \
     --plan test/ui-test-plan.md
   ```

4. Let the runner compile the project with Java 25, launch a fresh program process for each test case, and compare each case before continuing.
5. If a case fails, stop immediately. Report the failing case, its console input, the recorded console output, the normalized actual output, and the expected output. Do not run later cases.
6. If all cases pass, report every case and include the recorded console input and output for the full session.

## Test plan format

Use this format for each case:

```markdown
## Test case 1: Add a todo

- Aim: Verify that a todo is added and displayed correctly.

### Inputs

```text
todo borrow book
bye
```

### Expected output

```text
Got it. I've added this task:
  [T][ ] borrow book
Now you have 1 tasks in the list.
```
```

Put setup commands and the assertion command in the same `Inputs` block when a test needs state. The runner ignores the application's banner, greeting, farewell, prompt markers, separators, and blank lines when comparing output, but preserves and prints the visible console output in the session record.
