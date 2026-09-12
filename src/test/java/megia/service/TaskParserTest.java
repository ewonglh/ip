package megia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import megia.exception.ErrorCode;
import megia.exception.UserInputException;
import megia.model.Event;
import megia.model.ParsedCommand;

class TaskParserTest {
    private final TaskParser taskParser = new TaskParser();

    @Test
    void parseNewTask_sameDayEvent_returnsExpectedEvent() throws UserInputException {
        Event event = parseEvent("meeting /on 2/12/2019 /from 1400 /to 1600");

        assertEquals("meeting", event.getDescription());
        assertEquals(LocalDateTime.of(2019, 12, 2, 14, 0), event.getStartTime());
        assertEquals(LocalDateTime.of(2019, 12, 2, 16, 0), event.getEndTime());
    }

    @Test
    void parseNewTask_completeEndpoints_returnsExpectedEvent() throws UserInputException {
        Event event = parseEvent(
                "conference /from 2019-12-02 1400 /to 2019-12-03 1600");

        assertEquals("conference", event.getDescription());
        assertEquals(LocalDateTime.of(2019, 12, 2, 14, 0), event.getStartTime());
        assertEquals(LocalDateTime.of(2019, 12, 3, 16, 0), event.getEndTime());
    }

    @Test
    void parseNewTask_missingEventMarkers_preservesErrorPrecedence() {
        assertEventError("meeting /to 2019-12-02 1600",
                ErrorCode.EVENT_FROM_MARKER_MISSING);
        assertEventError("meeting /from 2019-12-02 1400",
                ErrorCode.EVENT_TO_MARKER_MISSING);
    }

    @Test
    void parseNewTask_duplicateEventMarker_preservesErrorPrecedence() {
        assertEventError("meeting /from 2019-12-02 1400 /from 2019-12-03 1400",
                ErrorCode.DUPLICATE_MARKER);
        assertEventError(
                "meeting /to 2019-12-02 1600 /to 2019-12-03 1600 /from 2019-12-02 1400",
                ErrorCode.DUPLICATE_MARKER);
    }

    @Test
    void parseNewTask_malformedEventMarkers_preservesErrorPrecedence() {
        assertEventError("meeting /frm 2019-12-02 1400 /to 2019-12-02 1600",
                ErrorCode.EVENT_FROM_MARKER_MISSING);
        assertEventError("meeting /from 2019-12-02 1400 /too 2019-12-02 1600",
                ErrorCode.EVENT_TO_MARKER_MISSING);
    }

    @Test
    void parseNewTask_outOfOrderEventMarkers_preservesErrorCode() {
        assertEventError(
                "meeting /to 2019-12-02 1600 /from 2019-12-02 1400",
                ErrorCode.EVENT_MARKERS_OUT_OF_ORDER);
        assertEventError(
                "meeting /from 2019-12-02 1400 /on 2019-12-02 /to 2019-12-02 1600",
                ErrorCode.EVENT_MARKERS_OUT_OF_ORDER);
    }

    @Test
    void parseNewTask_malformedEventValues_preservesErrorCodes() {
        assertEventError("meeting /on 30/2/2019 /from 1400 /to 1600",
                ErrorCode.EVENT_DATE_INVALID);
        assertEventError("meeting /on 2019-12-02 /from 2460 /to 1600",
                ErrorCode.EVENT_START_TIME_INVALID);
        assertEventError("meeting /from 2019-12-02 1400 /to 2019-12-02 2460",
                ErrorCode.EVENT_END_TIME_INVALID);
        assertEventError("meeting /on 2019-12-02 /from 1600 /to 1600",
                ErrorCode.EVENT_END_NOT_AFTER_START);
    }

    private Event parseEvent(String body) throws UserInputException {
        return assertInstanceOf(
                Event.class, taskParser.parseNewTask(new ParsedCommand("event", body)));
    }

    private void assertEventError(String body, ErrorCode expectedErrorCode) {
        ParsedCommand command = new ParsedCommand("event", body);
        UserInputException exception = assertThrows(
                UserInputException.class, () -> taskParser.parseNewTask(command));
        assertEquals(expectedErrorCode, exception.getErrorCode());
    }
}
