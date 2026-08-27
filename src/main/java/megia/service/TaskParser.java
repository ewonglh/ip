package megia.service;

import megia.exception.ErrorCode;
import megia.exception.UserInputException;
import megia.model.Deadline;
import megia.model.ParsedCommand;
import megia.model.Task;
import megia.model.Todo;
import megia.model.Event;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses complete user input and creates task objects from parsed commands.
 * Dates and times remain strings because the application does not require
 * date/time calculations.
 */
public final class TaskParser {
    private static final String MARKER_BY = "/by";
    private static final String MARKER_FROM = "/from";
    private static final String MARKER_TO = "/to";
    private static final Pattern MARKER_BY_PATTERN = markerPattern(MARKER_BY);
    private static final Pattern MARKER_FROM_PATTERN = markerPattern(MARKER_FROM);
    private static final Pattern MARKER_TO_PATTERN = markerPattern(MARKER_TO);

    /**
     * Creates a parser for task commands.
     */
    public TaskParser() {
    }

    /**
     * Splits complete user input into its command name and body.
     *
     * @param rawCommand Complete user input to parse.
     * @return Parsed command containing the command name and body.
     */
    public ParsedCommand parseCommand(String rawCommand) {
        String trimmedInput = rawCommand.strip();
        String[] inputParts = trimmedInput.split("\\s+", 2);
        return new ParsedCommand(
                inputParts[0],
                inputParts.length > 1 ? inputParts[1] : ""
        );
    }

    /**
     * Parses the body of a task creation commandName.
     *
     * @param command Task commandName, such as {@code todo} or {@code event}.
     * @return Parsed task.
     * @throws UserInputException If the body does not follow the commandName syntax.
     */
    public Task parseNewTask(ParsedCommand command) throws UserInputException {
        return switch (command.commandName()) {
            case "deadline" -> parseDeadline(command.body());
            case "event" -> parseEvent(command.body());
            default -> parseTodo(command.body());
        };
    }

    /**
     * Parses and validates a positive, one-based task ID.
     *
     * @param command Command that needs the ID, such as {@code mark}.
     * @return Parsed task ID.
     * @throws UserInputException If the ID is missing or invalid.
     */
    public int parseTaskId(ParsedCommand command) throws UserInputException {
        String taskIdText = command.body().strip();
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
        } catch (NumberFormatException exception) {
            throw new UserInputException(ErrorCode.TASK_ID_TOO_LARGE, taskIdText);
        }
    }

    private Todo parseTodo(String body) throws UserInputException {
        String description = body.trim();
        if (description.isBlank()) {
            throw new UserInputException(ErrorCode.TODO_DESCRIPTION_MISSING);
        }
        return new Todo(description);
    }

    /**
     * Parses a deadline body into its description and deadline text.
     */
    private Deadline parseDeadline(String body) throws UserInputException {
        MarkerLocation byMarker = findSingleMarker(
                body, MARKER_BY_PATTERN, MARKER_BY, ErrorCode.DEADLINE_BY_MARKER_MISSING);

        String description = body.substring(0, byMarker.start()).strip();
        String deadline = body.substring(byMarker.end()).strip();

        if (description.isBlank()) {
            throw new UserInputException(ErrorCode.DEADLINE_DESCRIPTION_MISSING);
        }

        if (deadline.isBlank()) {
            throw new UserInputException(ErrorCode.DEADLINE_BY_VALUE_MISSING);
        }

        return new Deadline(description, deadline);
    }

    /**
     * Parses an event body into its description, start time, and end time.
     */
    private Event parseEvent(String body) throws UserInputException {
        MarkerLocation fromMarker = findSingleMarker(
                body, MARKER_FROM_PATTERN, MARKER_FROM, ErrorCode.EVENT_FROM_MARKER_MISSING);
        MarkerLocation toMarker = findSingleMarker(
                body, MARKER_TO_PATTERN, MARKER_TO, ErrorCode.EVENT_TO_MARKER_MISSING);

        if (fromMarker.start() > toMarker.start()) {
            throw new UserInputException(ErrorCode.EVENT_MARKERS_OUT_OF_ORDER);
        }

        String description = body.substring(0, fromMarker.start()).strip();
        String startTime = body.substring(fromMarker.end(), toMarker.start()).strip();
        String endTime = body.substring(toMarker.end()).strip();

        if (description.isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_DESCRIPTION_MISSING);
        }

        if (startTime.isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_FROM_VALUE_MISSING);
        }

        if (endTime.isBlank()) {
            throw new UserInputException(ErrorCode.EVENT_TO_VALUE_MISSING);
        }

        return new Event(description, startTime, endTime);
    }

    /**
     * Finds exactly one whitespace-delimited marker in a commandName body.
     */
    private MarkerLocation findSingleMarker(
            String body,
            Pattern pattern,
            String marker,
            ErrorCode missingMarkerError) throws UserInputException {
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
     * Stores the character range occupied by a commandName marker.
     */
    private record MarkerLocation(int start, int end) {
    }
}
