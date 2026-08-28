# Megia

Megia is a command-line task manager written in Java 25. It supports todos,
deadlines, and events, with localized and corrective error messages for invalid
input.

## Features

- Add todos, deadlines, and events
- List stored tasks
- Mark and unmark tasks
- English and Chinese localization
- Specific error messages that explain how to correct invalid commands

## Commands

| Command | Example |
|---|---|
| `todo <description>` | `todo borrow a book` |
| `deadline <description> /by <date and time>` | `deadline submit report /by 2019-12-02 1800` |
| `event <description> /on <date> /from <time> /to <time>` | `event meeting /on 2019-12-02 /from 1400 /to 1600` |
| `event <description> /from <date and time> /to <date and time>` | `event conference /from 2019-12-02 1400 /to 2019-12-03 1600` |
| `list` | `list` |
| `list <date>` | `list 2/12/2019` |
| `find <query>` | `find borrow a book` |
| `mark <task number>` | `mark 1` |
| `unmark <task number>` | `unmark 1` |
| `delete <task number>` | `delete 1` |
| `bye` | `bye` |

Dates use `YYYY-MM-DD` or day-first `D/M/YYYY`; times use 24-hour `HHmm`.
Deadlines and event endpoints are persisted as ISO local date-times.

`list <date>` shows deadlines on that date and events spanning that calendar
date. Todos do not appear in date-filtered lists, and task numbers remain the
same as in an unfiltered list.

`find <query>` lists every task whose description contains the query. Matching
is literal and case-sensitive; spaces and special characters are supported, and
task numbers remain the same as in an unfiltered list.

## Requirements

- JDK 25
- A recent version of IntelliJ IDEA, or a terminal with Java configured

## Running in IntelliJ IDEA

1. Open the repository in IntelliJ IDEA.
2. Configure the project SDK and language level to Java 25.
3. Open `src/main/java/megia/Megia.java`.
4. Run `Megia.main()`.

Keep `src/main/java` as the Java source root.

## Running from the terminal

```bash
javac --release 25 -d out $(find src/main/java -name '*.java')
java --class-path out:src/main/resources megia.Megia
```

## Running the console tests

```bash
python3 .codex/skills/test-ui/scripts/run_ui_tests.py \
  --plan test/ui-test-plan.md
```

The test runner compiles the project using Java 25 and executes each console
test in a fresh process.

## Localization

Set the language in `src/main/resources/application.properties`:

```properties
language=en
```

Use `en` for English or `cn` for Chinese. If the configuration cannot be
loaded, Megia falls back to English.

## AI use declaration
- Coding harness/agent: Codex CLI
- Used to discuss error-planning architecture and plan increment Level-5
  - Used to review and suggest improvements to iterate on Level-5 requirements
- Used to give flavour to chatbot responses
- Used to review and tweak JavaDoc comments
- Used to expand the console UI tests with `test-ui` skill
