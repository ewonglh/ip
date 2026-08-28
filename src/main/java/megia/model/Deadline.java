package megia.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a task that should be completed by a specified date or time.
 */
public class Deadline extends Task {
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd uuuu, h:mm a", Locale.ENGLISH);
    private final LocalDateTime deadline;

    /**
     * Creates a deadline task with its description and deadline text.
     *
     * @param description Description of the task.
     * @param deadline Deadline date and time.
     */
    public Deadline(String description, LocalDateTime deadline) {
        super(description);
        this.deadline = Objects.requireNonNull(deadline);
    }

    /**
     * Restores a deadline task with its description, completion status, and deadline text.
     *
     * @param description Description of the task.
     * @param isDone Completion status of the task.
     * @param deadline Deadline date and time.
     */
    public Deadline(String description, boolean isDone, LocalDateTime deadline) {
        super(description, isDone);
        this.deadline = Objects.requireNonNull(deadline);
    }

    @Override
    public String toString() {
        return "[D]" + super.toString()
                + " (by: " + deadline.format(DISPLAY_FORMATTER) + ")";
    }

    @Override
    public String encode() {
        return TaskType.DEADLINE.name() + "," + super.encode() + "," + deadline;
    }
}
