# Megia user guide

Megia is a Java 25 task manager with a console interface and a JavaFX desktop
interface. It stores todos, deadlines, and events locally, and supports English
and Chinese messages.

## Requirements and launch

Install a Java Development Kit (JDK) 25. To obtain a packaged JAR from a source
checkout, run the Gradle wrapper:

```bash
./gradlew shadowJar
```

The packaged application is `build/libs/megia.jar`. Run its default console
interface with:

```bash
java -jar build/libs/megia.jar
```

On a desktop-capable machine, launch the JavaFX interface from the checkout with:

```bash
./gradlew runGui
```

The packaged JAR also contains the GUI launcher:

```bash
java -cp build/libs/megia.jar megia.ui.GuiLauncher
```

The GUI has the same command syntax as the console. Type a command in the
composer and press Enter or select Send. The starter buttons fill in common
commands without executing them.

## Commands

Task numbers are one-based. `list` shows the numbers to use with `mark`,
`unmark`, and `delete`.

| Command | Valid example | Expected outcome |
| --- | --- | --- |
| `help` | `help` | Shows all commands and date/time formats in the active language. |
| `todo <description>` | `todo read the Java guide` | Adds an incomplete todo and reports the new task count. |
| `deadline <description> /by <date and time>` | `deadline submit report /by 2026-12-02 1800` | Adds an incomplete deadline due at the supplied date and time. |
| `event <description> /on <date> /from <time> /to <time>` | `event project meeting /on 2/12/2026 /from 1400 /to 1600` | Adds an event on one date with the supplied start and end times. |
| `event <description> /from <date and time> /to <date and time>` | `event conference /from 2026-12-02 1400 /to 2026-12-03 1600` | Adds an event whose endpoints may be on different dates. |
| `list` | `list` | Displays every stored task with its current task number. |
| `list <date>` | `list 2026-12-02` | Displays deadlines on that date and events spanning that date; todos are excluded. |
| `find <query>` | `find report` | Displays tasks whose descriptions contain the literal query. |
| `mark <task number>` | `mark 1` | Marks task 1 as completed. |
| `unmark <task number>` | `unmark 1` | Marks task 1 as not completed. |
| `delete <task number>` | `delete 1` | Removes task 1. The GUI asks for confirmation before deleting. |
| `bye` | `bye` | Saves the tasks and exits. In the GUI, the window closes after the save succeeds. |

Successful additions, marks, unmarks, and deletions are saved as they are
performed. The console prints a confirmation and the GUI adds a confirmation
message and task card to the transcript.

## Dates, times, and searches

- Dates must be valid calendar dates in `YYYY-MM-DD` or day-first `D/M/YYYY`,
  such as `2026-12-02` or `2/12/2026`.
- Times use strict 24-hour `HHmm`, such as `0900`, `1400`, or `1800`.
- Combine a date and time with a space. Invalid dates and times are rejected.
- An event's end must be strictly later than its start.
- `list <date>` includes an event when it overlaps that calendar date. Its task
  number remains the same as in an unfiltered `list`; todos do not appear in a
  date-filtered list.
- `find` performs a literal, case-sensitive substring search. For example,
  `find Report` does not match a description containing `report`. Spaces and
  punctuation in the query are allowed. Search results retain their original
  task numbers.

## Language and profile image

In the GUI, choose English or Chinese from the Language selector. The interface
updates immediately, and the choice is saved in the user's Java Preferences for
future runs. The console uses the same active language but has no selector.

For the initial setting, edit `language=en` or `language=cn` in
`src/main/resources/application.properties` before packaging or running from a
checkout. A saved GUI preference takes precedence on later launches; invalid or
unavailable settings fall back to English.

To change the GUI's user avatar, select Choose profile image and choose a
readable PNG, JPG/JPEG, GIF, or BMP file. Megia stores the normalized path in
Java Preferences and does not copy the image into task storage. If the file is
moved or becomes unreadable, Megia uses the default avatar until another image
is selected.

## Storage and recovery

By default, tasks are stored in `./task_storage.csv`, relative to the directory
from which Megia is launched. The path is configured by
`storage.task.path` in `src/main/resources/application.properties`. The file is
created when Megia first saves tasks. Language and profile-image preferences are
kept separately in Java Preferences.

Task descriptions may contain commas and double quotation marks. For example:

```text
todo read "Java", second edition
```

The description is preserved when the CSV file is saved and loaded again. A
description must stay on one line; line breaks are rejected.

If an existing task file is unreadable or contains malformed data, Megia reports
the affected path (and malformed line, when available) instead of overwriting
it. In the GUI, commands and saving are disabled until you repair or remove the
file externally and restart Megia. In the console, the error is printed and the
application stops; repair the file and restart it.

When the GUI cannot save during `bye` or window closing, it keeps the window and
task controls active and shows an error. Check that the storage folder is
writable, then retry `bye` or close the window again. The console reports a save
error before it exits; fix the folder and start Megia again to retry.
