# UI Test Plan

The test-ui skill gives each test case isolated storage. Each session runs in a fresh process, while numbered sessions in the same test case share that storage. Include `bye` as the final input in every session so the application exits cleanly.

## Test case 1: Add a todo

- Aim: Verify that a todo is added and displayed with the correct type and task count.

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

## Test case 2: Add a deadline with a string date

- Aim: Verify that a deadline preserves a multi-word date/time string without converting it.

### Inputs

```text
deadline do homework /by no idea :-p
bye
```

### Expected output

```text
Got it. I've added this task:
  [D][ ] do homework (by: no idea :-p)
Now you have 1 tasks in the list.
```

## Test case 3: Add an event with start and end strings

- Aim: Verify that an event separates its description, start time, and end time correctly.

### Inputs

```text
event project meeting /from Mon 2pm /to 4pm
bye
```

### Expected output

```text
Got it. I've added this task:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 1 tasks in the list.
```

## Test case 4: List and mark tasks

- Aim: Verify that multiple task types are listed in insertion order and that marking updates completion status.

### Inputs

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
mark 1
list
bye
```

### Expected output

```text
Got it. I've added this task:
  [T][ ] read book
Now you have 1 tasks in the list.
Got it. I've added this task:
  [D][ ] return book (by: June 6th)
Now you have 2 tasks in the list.
Got it. I've added this task:
  [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
Now you have 3 tasks in the list.
Nice! I've marked this task as done:
[T][X] read book
Here's your tasks:
1.[T][X] read book
2.[D][ ] return book (by: June 6th)
3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
```

## Test case 5: Reject a todo without a description

- Aim: Verify that an empty todo explains how to supply a description.

### Inputs

```text
todo
bye
```

### Expected output

```text
A todo needs a description. Try: todo borrow a book
```

## Test case 6: Reject a deadline without a by marker

- Aim: Verify that a missing or lookalike `/by` marker produces corrective syntax guidance.

### Inputs

```text
deadline submit report /bye Friday
bye
```

### Expected output

```text
A deadline needs "/by" followed by its date or time. Try: deadline submit report /by Friday 5pm
```

## Test case 7: Reject a deadline without a description

- Aim: Verify that a deadline description is required before `/by`.

### Inputs

```text
deadline /by Friday
bye
```

### Expected output

```text
A deadline needs a description before "/by". Try: deadline submit report /by Friday 5pm
```

## Test case 8: Reject a deadline without a by value

- Aim: Verify that `/by` at the end of a deadline requests a date or time.

### Inputs

```text
deadline submit report /by
bye
```

### Expected output

```text
Enter a date or time after "/by". Try: deadline submit report /by Friday 5pm
```

## Test case 9: Reject an event without a from marker

- Aim: Verify that a missing or lookalike `/from` marker is identified specifically.

### Inputs

```text
event meeting /fromage 2pm /to 4pm
bye
```

### Expected output

```text
An event needs "/from" followed by its start time. Try: event meeting /from 2pm /to 4pm
```

## Test case 10: Reject an event without a to marker

- Aim: Verify that a missing `/to` marker is identified specifically.

### Inputs

```text
event meeting /from 2pm
bye
```

### Expected output

```text
An event needs "/to" followed by its end time. Try: event meeting /from 2pm /to 4pm
```

## Test case 11: Reject event markers in reverse order

- Aim: Verify that reversed event markers explain the required order.

### Inputs

```text
event meeting /to 4pm /from 2pm
bye
```

### Expected output

```text
Place "/from" before "/to". Try: event meeting /from 2pm /to 4pm
```

## Test case 12: Reject an event without a description

- Aim: Verify that an event description is required before `/from`.

### Inputs

```text
event /from 2pm /to 4pm
bye
```

### Expected output

```text
An event needs a description before "/from". Try: event meeting /from 2pm /to 4pm
```

## Test case 13: Reject an event without a from value

- Aim: Verify that an empty `/from` value requests a start time.

### Inputs

```text
event meeting /from /to 4pm
bye
```

### Expected output

```text
Enter a start time after "/from". Try: event meeting /from 2pm /to 4pm
```

## Test case 14: Reject an event without a to value

- Aim: Verify that `/to` at the end of an event requests an end time.

### Inputs

```text
event meeting /from 2pm /to
bye
```

### Expected output

```text
Enter an end time after "/to". Try: event meeting /from 2pm /to 4pm
```

## Test case 15: Reject a duplicate marker

- Aim: Verify that duplicate syntax markers identify which marker must be removed.

### Inputs

```text
deadline submit report /by Friday /by Saturday
bye
```

### Expected output

```text
"/by" can only appear once. Remove the extra marker and try again.
```

## Test case 16: Reject a missing task ID

- Aim: Verify that a mark command without an ID includes a valid example.

### Inputs

```text
mark
bye
```

### Expected output

```text
Enter the task number to mark. Try: mark 1
```

## Test case 17: Reject a nonnumeric task ID

- Aim: Verify that the invalid task ID is echoed with the required value type.

### Inputs

```text
mark abc
bye
```

### Expected output

```text
"abc" is not a valid task number. Enter a positive whole number.
```

## Test case 18: Reject a non-positive task ID

- Aim: Verify that task ID zero explains where task numbering starts.

### Inputs

```text
mark 0
mark -2
bye
```

### Expected output

```text
Task numbers start from 1. Use "list" to see the available task numbers.
Task numbers start from 1. Use "list" to see the available task numbers.
```

## Test case 19: Reject an overflowing task ID

- Aim: Verify that an integer larger than the supported range is identified separately.

### Inputs

```text
mark 999999999999999999999999999999999999
bye
```

### Expected output

```text
The task number "999999999999999999999999999999999999" is too large. Use "list" to see the available task numbers.
```

## Test case 20: Explain an operation on an empty task list

- Aim: Verify that marking an empty list suggests adding a task first.

### Inputs

```text
mark 1
bye
```

### Expected output

```text
There are no tasks to mark. Add one first, for example: todo borrow a book
```

## Test case 21: Explain an out-of-range task ID

- Aim: Verify that a missing task reports both the requested ID and valid range.

### Inputs

```text
todo first task
mark 2
bye
```

### Expected output

```text
Got it. I've added this task:
  [T][ ] first task
Now you have 1 tasks in the list.
Task 2 does not exist. Choose a number from 1 to 1, or use "list" to view your tasks.
```

## Test case 22: Explain an unknown command

- Aim: Verify that an unknown command is echoed alongside the available commands.

### Inputs

```text
remember borrow book
bye
```

### Expected output

```text
"remember" is not a recognized command. Available commands: todo, deadline, event, list, mark, unmark, delete, bye.
```

## Test case 23: Accept trailing arguments for list and bye

- Aim: Verify that `list` and `bye` remain permissive about trailing arguments.

### Inputs

```text
todo borrow book
list ignored arguments
bye ignored arguments
bye
```

### Expected output

```text
Got it. I've added this task:
  [T][ ] borrow book
Now you have 1 tasks in the list.
Here's your tasks:
1.[T][ ] borrow book
```

## Test case 24: Explain an empty command

- Aim: Verify that blank input suggests a useful next command.

### Inputs

```text
todo temporary task

bye
```

### Expected output

```text
Got it. I've added this task:
  [T][ ] temporary task
Now you have 1 tasks in the list.
You didn't enter a command. Try "list" to view your tasks.
```

## Test case 25: Delete a task and reindex the list

- Aim: Verify that deleting a middle task returns it and reindexes the remaining tasks.

### Inputs

```text
todo read book
deadline return book /by Friday
event project meeting /from 2pm /to 4pm
delete 2
list
bye
```

### Expected output

```text
Got it. I've added this task:
  [T][ ] read book
Now you have 1 tasks in the list.
Got it. I've added this task:
  [D][ ] return book (by: Friday)
Now you have 2 tasks in the list.
Got it. I've added this task:
  [E][ ] project meeting (from: 2pm to: 4pm)
Now you have 3 tasks in the list.
I've removed this task:
[D][ ] return book (by: Friday)
Here's your tasks:
1.[T][ ] read book
2.[E][ ] project meeting (from: 2pm to: 4pm)
```

## Test case 26: Delete the final task

- Aim: Verify that deleting the only task leaves an empty task list.

### Inputs

```text
todo only task
delete 1
list
bye
```

### Expected output

```text
Got it. I've added this task:
  [T][ ] only task
Now you have 1 tasks in the list.
I've removed this task:
[T][ ] only task
You have no tasks.
```

## Test case 27: Reject malformed delete IDs

- Aim: Verify that missing and nonnumeric delete IDs provide corrective guidance.

### Inputs

```text
delete
delete abc
bye
```

### Expected output

```text
Enter the task number to delete. Try: delete 1
"abc" is not a valid task number. Enter a positive whole number.
```

## Test case 28: Reject unavailable delete IDs

- Aim: Verify that deletion distinguishes an empty list from an out-of-range task ID.

### Inputs

```text
delete 1
todo first task
delete 2
bye
```

### Expected output

```text
There are no tasks to delete. Add one first, for example: todo borrow a book
Got it. I've added this task:
  [T][ ] first task
Now you have 1 tasks in the list.
Task 2 does not exist. Choose a number from 1 to 1, or use "list" to view your tasks.
```

## Test case 29: Persist tasks across restarts

- Aim: Verify that task types, order, and completion status survive an application restart.

### Inputs (session 1)

```text
todo read book
deadline return book /by Friday 5pm
event project meeting /from Monday 2pm /to 4pm
mark 1
bye
```

### Expected output (session 1)

```text
Got it. I've added this task:
  [T][ ] read book
Now you have 1 tasks in the list.
Got it. I've added this task:
  [D][ ] return book (by: Friday 5pm)
Now you have 2 tasks in the list.
Got it. I've added this task:
  [E][ ] project meeting (from: Monday 2pm to: 4pm)
Now you have 3 tasks in the list.
Nice! I've marked this task as done:
[T][X] read book
```

### Inputs (session 2)

```text
list
bye
```

### Expected output (session 2)

```text
Here's your tasks:
1.[T][X] read book
2.[D][ ] return book (by: Friday 5pm)
3.[E][ ] project meeting (from: Monday 2pm to: 4pm)
```
