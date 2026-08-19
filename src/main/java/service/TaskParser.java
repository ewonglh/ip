package service;

import model.Deadline;
import model.Event;
import model.Task;
import model.ToDo;

/**
 * Converts task command bodies into the corresponding task objects.
 * Dates and times remain strings because the application does not require
 * date/time calculations.
 */
public final class TaskParser {

    private static final String BY_MARKER = " /by ";
    private static final String FROM_MARKER = " /from ";
    private static final String TO_MARKER = " /to ";

    /**
     * Parses the body of a task command.
     *
     * @param command task command, such as {@code todo} or {@code event}
     * @param body input after the command name
     * @return the parsed task
     */
    public Task parse(String command, String body) {
        return switch (command) {
            case "todo" -> new ToDo(body);
            case "deadline" -> parseDeadline(body);
            case "event" -> parseEvent(body);
            default -> throw new IllegalArgumentException(
                    "Unknown task command: " + command);
        };
    }

    /**
     * Parses a deadline body into its description and deadline text.
     */
    private Deadline parseDeadline(String body) {
        int byIndex = body.indexOf(BY_MARKER);
        if (byIndex < 0) {
            throw new IllegalArgumentException("Deadline is missing /by.");
        }

        String description = body.substring(0, byIndex);
        String by = body.substring(byIndex + BY_MARKER.length());
        return new Deadline(description, by);
    }

    /**
     * Parses an event body into its description, start time, and end time.
     */
    private Event parseEvent(String body) {
        int fromIndex = body.indexOf(FROM_MARKER);
        int toIndex = body.indexOf(TO_MARKER, fromIndex + FROM_MARKER.length());
        if (fromIndex < 0 || toIndex < 0) {
            throw new IllegalArgumentException(
                    "Event must contain /from and /to.");
        }

        String description = body.substring(0, fromIndex);
        String from = body.substring(fromIndex + FROM_MARKER.length(), toIndex);
        String to = body.substring(toIndex + TO_MARKER.length());
        return new Event(description, from, to);
    }
}
