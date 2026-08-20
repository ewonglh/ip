package service;

import exception.ErrorCode;
import exception.UserInputException;
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
    public Task parse(String command, String body) throws UserInputException {
        return switch (command) {
            case "todo" -> parseToDo(body);
            case "deadline" -> parseDeadline(body);
            case "event" -> parseEvent(body);
            default -> throw new UserInputException(ErrorCode.TASK_NOT_FOUND);
        };
    }

    public int parseTaskId(String body) throws UserInputException {
        try {
            return Integer.parseInt(body);
        } catch (NumberFormatException e) {
            throw new UserInputException(ErrorCode.TASK_ID_NOT_INTEGER);
        }
    }

    private ToDo parseToDo(String body) throws UserInputException {
        body = body.trim();
        if (body.isBlank()) {
            throw new UserInputException(ErrorCode.TODO_DESCRIPTION_MISSING);
        }
        return new ToDo(body);
    }

    /**
     * Parses a deadline body into its description and deadline text.
     */
    private Deadline parseDeadline(String body) throws UserInputException {
        int byIndex = body.indexOf(BY_MARKER);
        if (byIndex < 0) {
            throw new UserInputException(ErrorCode.DEADLINE_BY_MARKER_MISSING);
        }

        String description = body.substring(0, byIndex).strip();
        String by = body.substring(byIndex + BY_MARKER.length()).strip();

        if (description.strip().isBlank()) {
            throw new UserInputException(ErrorCode.DEADLINE_DESCRIPTION_MISSING);
        }

        if (by.strip().isBlank()) {
            throw new UserInputException(ErrorCode.DEADLINE_BY_VALUE_MISSING);
        }

        return new Deadline(description, by);
    }

    /**
     * Parses an event body into its description, start time, and end time.
     */
    private Event parseEvent(String body) throws UserInputException {
        int fromIndex = body.indexOf(FROM_MARKER);
        int toIndex = body.indexOf(TO_MARKER);
        if (fromIndex < 0 || toIndex < 0) {
            throw new UserInputException(ErrorCode.EVENT_TO_MARKER_MISSING);
        }

        if (fromIndex > toIndex) {
            throw new UserInputException(ErrorCode.EVENT_MARKERS_OUT_OF_ORDER);
        }

        String description = body.substring(0, fromIndex);
        String from = body.substring(fromIndex + FROM_MARKER.length(), toIndex).trim();
        String to = body.substring(toIndex + TO_MARKER.length()).trim();

        if (description.strip().isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_DESCRIPTION_MISSING);
        }

        if (from.strip().isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_FROM_VALUE_MISSING);
        }

        if (to.strip().isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_TO_VALUE_MISSING);
        }

        return new Event(description, from, to);
    }
}