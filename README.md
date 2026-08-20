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
| `deadline <description> /by <date>` | `deadline submit report /by Friday 5pm` |
| `event <description> /from <start> /to <end>` | `event meeting /from 2pm /to 4pm` |
| `list` | `list` |
| `mark <task number>` | `mark 1` |
| `unmark <task number>` | `unmark 1` |
| `bye` | `bye` |

Dates and times are stored as free-form text. Trailing arguments supplied to
`list` and `bye` are ignored.

## Requirements

- JDK 25
- A recent version of IntelliJ IDEA, or a terminal with Java configured

## Running in IntelliJ IDEA

1. Open the repository in IntelliJ IDEA.
2. Configure the project SDK and language level to Java 25.
3. Open `src/main/java/Megia.java`.
4. Run `Megia.main()`.

Keep `src/main/java` as the Java source root.

## Running from the terminal

```bash
java --class-path src/main/java:src/main/resources src/main/java/Megia.java
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

I used Codex to discuss the error-handling architecture, plan the increment,
implement structured and localized error handling, identify edge cases, and
expand the console UI tests. I reviewed the generated changes and verified
them using the project's Java 25 test runner.
