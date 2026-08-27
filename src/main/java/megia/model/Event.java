package megia.model;

/**
 * Represents a task that occurs between specified start and end times.
 */
public class Event extends Task {
    private final String startTime;
    private final String endTime;

    /**
     * Creates an event task with its description and time range.
     *
     * @param description Description of the event.
     * @param startTime Start time stored as free-form text.
     * @param endTime End time stored as free-form text.
     */
    public Event(String description, String startTime, String endTime) {
        super(description);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Restores an event task with its description, completion status, and time range.
     *
     * @param description Description of the event.
     * @param isDone Completion status of the event.
     * @param startTime Start time stored as free-form text.
     * @param endTime End time stored as free-form text.
     */
    public Event(String description, boolean isDone, String startTime, String endTime) {
        super(description, isDone);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + startTime + " to: " + endTime + ")";
    }

    @Override
    public String encode() {
        return TaskType.EVENT.name() + "," + super.encode() + "," + startTime + "," + endTime;
    }
}
