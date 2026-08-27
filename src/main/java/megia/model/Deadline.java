package megia.model;

/**
 * Represents a task that should be completed by a specified date or time.
 */
public class Deadline extends Task {
    private final String deadline;

    /**
     * Creates a deadline task with its description and deadline text.
     *
     * @param description Description of the task.
     * @param deadline Deadline date or time stored as free-form text.
     */
    public Deadline(String description, String deadline) {
        super(description);
        this.deadline = deadline;
    }

    public Deadline(String description, boolean isDone, String deadline) {
        super(description, isDone);
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString()
                + " (by: " + deadline + ")";
    }

    @Override
    public String encode() {
        return TaskType.DEADLINE.name() + "," + super.encode() + "," + deadline;
    }
}