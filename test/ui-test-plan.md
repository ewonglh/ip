# UI Test Plan

The test-ui skill runs each test case in a fresh process. Include bye as the final input in every case so the application exits cleanly.

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
