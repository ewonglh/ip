package megia.model;

/**
 * Represents a task description and its completion state.
 */
public class Task {
    private final String description;
    private boolean isDone;

    /**
     * Creates an incomplete task with the specified description.
     *
     * @param description Description of the task.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Marks this task as completed.
     */
    public void markAsDone() {
        this.isDone = true;
    }

    /**
     * Marks this task as incomplete.
     */
    public void markAsNotDone() {
        this.isDone = false;
    }

    @Override
    public String toString() {
        return String.format("[%c] %s", (isDone ? 'X' : ' '), description);
    }

    /**
     * Returns whether this task has been completed.
     *
     * @return True when the task is complete.
     */
    public boolean isDone() {
        return isDone;
    }
}
