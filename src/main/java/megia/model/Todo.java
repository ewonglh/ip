package megia.model;

/**
 * Represents a task without a deadline or scheduled time range.
 */
public class Todo extends Task {
    /**
     * Creates a todo task with the specified description.
     *
     * @param description Description of the task.
     */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
