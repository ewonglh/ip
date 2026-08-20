package service;

import exception.ErrorCode;
import exception.UserInputException;
import model.Deadline;
import model.Event;
import model.Task;
import model.ToDo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts task command bodies into the corresponding task objects.
 * Dates and times remain strings because the application does not require
 * date/time calculations.
 */
public final class TaskParser {

    private static final String BY_MARKER = "/by";
    private static final String FROM_MARKER = "/from";
    private static final String TO_MARKER = "/to";
    private static final Pattern BY_MARKER_PATTERN = markerPattern(BY_MARKER);
    private static final Pattern FROM_MARKER_PATTERN = markerPattern(FROM_MARKER);
    private static final Pattern TO_MARKER_PATTERN = markerPattern(TO_MARKER);

    /**
     * Parses the body of a task command.
     *
     * @param command task command, such as {@code todo} or {@code event}
     * @param body input after the command name
     * @return the parsed task
     * @throws UserInputException if the body does not follow the command syntax
     */
    public Task parse(String command, String body) throws UserInputException {
        return switch (command) {
            case "deadline" -> parseDeadline(body);
            case "event" -> parseEvent(body);
            default -> parseToDo(body);
        };
    }

    /**
     * Parses and validates a positive, one-based task ID.
     *
     * @param body input after the command name
     * @param command command that needs the ID, such as {@code mark}
     * @return the parsed task ID
     * @throws UserInputException if the ID is missing or invalid
     */
    public int parseTaskId(String body, String command) throws UserInputException {
        String taskIdText = body.strip();
        if (taskIdText.isEmpty()) {
            throw new UserInputException(ErrorCode.TASK_ID_MISSING, command);
        }
        if (!taskIdText.matches("[+-]?\\d+")) {
            throw new UserInputException(ErrorCode.TASK_ID_NOT_INTEGER, taskIdText);
        }
        if (taskIdText.startsWith("-")) {
            throw new UserInputException(ErrorCode.TASK_ID_NOT_POSITIVE);
        }

        try {
            int taskId = Integer.parseInt(taskIdText);
            if (taskId <= 0) {
                throw new UserInputException(ErrorCode.TASK_ID_NOT_POSITIVE);
            }
            return taskId;
        } catch (NumberFormatException e) {
            throw new UserInputException(ErrorCode.TASK_ID_TOO_LARGE, taskIdText);
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
        MarkerLocation byMarker = findSingleMarker(
                body, BY_MARKER_PATTERN, BY_MARKER, ErrorCode.DEADLINE_BY_MARKER_MISSING);

        String description = body.substring(0, byMarker.start()).strip();
        String by = body.substring(byMarker.end()).strip();

        if (description.isBlank()) {
            throw new UserInputException(ErrorCode.DEADLINE_DESCRIPTION_MISSING);
        }

        if (by.isBlank()) {
            throw new UserInputException(ErrorCode.DEADLINE_BY_VALUE_MISSING);
        }

        return new Deadline(description, by);
    }

    /**
     * Parses an event body into its description, start time, and end time.
     */
    private Event parseEvent(String body) throws UserInputException {
        MarkerLocation fromMarker = findSingleMarker(
                body, FROM_MARKER_PATTERN, FROM_MARKER, ErrorCode.EVENT_FROM_MARKER_MISSING);
        MarkerLocation toMarker = findSingleMarker(
                body, TO_MARKER_PATTERN, TO_MARKER, ErrorCode.EVENT_TO_MARKER_MISSING);

        if (fromMarker.start() > toMarker.start()) {
            throw new UserInputException(ErrorCode.EVENT_MARKERS_OUT_OF_ORDER);
        }

        String description = body.substring(0, fromMarker.start()).strip();
        String from = body.substring(fromMarker.end(), toMarker.start()).strip();
        String to = body.substring(toMarker.end()).strip();

        if (description.isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_DESCRIPTION_MISSING);
        }

        if (from.isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_FROM_VALUE_MISSING);
        }

        if (to.isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_TO_VALUE_MISSING);
        }

        return new Event(description, from, to);
    }

    /**
     * Finds exactly one whitespace-delimited marker in a command body.
     */
    private MarkerLocation findSingleMarker(
            String body,
            Pattern pattern,
            String marker,
            ErrorCode missingMarkerError
    ) throws UserInputException {
        Matcher matcher = pattern.matcher(body);
        if (!matcher.find()) {
            throw new UserInputException(missingMarkerError);
        }

        MarkerLocation location = new MarkerLocation(matcher.start(), matcher.end());
        if (matcher.find()) {
            throw new UserInputException(ErrorCode.DUPLICATE_MARKER, marker);
        }
        return location;
    }

    /**
     * Creates a pattern that matches a complete marker token rather than a
     * marker-like substring such as {@code /bye}.
     */
    private static Pattern markerPattern(String marker) {
        return Pattern.compile("(?<!\\S)" + Pattern.quote(marker) + "(?!\\S)");
    }

    /**
     * Stores the character range occupied by a command marker.
     */
    private record MarkerLocation(int start, int end) {
    }
}