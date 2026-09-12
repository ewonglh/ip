package megia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import megia.exception.StorageException;
import megia.model.Deadline;
import megia.model.Event;
import megia.model.Task;
import megia.model.TaskStorage;
import megia.model.Todo;

class LocalStorageServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void loadTaskData_allTaskTypes_preservesPersistedFields() throws Exception {
        Path storagePath = temporaryDirectory.resolve("tasks.csv");
        Files.writeString(storagePath, String.join(System.lineSeparator(),
                "TODO,true,borrow book",
                "DEADLINE,false,submit report,2026-09-10T18:00",
                "EVENT,true,conference,2026-09-10T09:00,2026-09-11T17:00"));
        LocalStorageService storageService = new LocalStorageService(storagePath.toString());

        TaskStorage taskStorage = storageService.loadTaskData().orElseThrow();

        assertEquals(3, taskStorage.getTaskCount());
        Todo todo = assertInstanceOf(Todo.class, taskStorage.getTaskEntries().get(0).task());
        assertTrue(todo.isDone());
        assertEquals("borrow book", todo.getDescription());

        Deadline deadline = assertInstanceOf(
                Deadline.class, taskStorage.getTaskEntries().get(1).task());
        assertFalse(deadline.isDone());
        assertEquals("submit report", deadline.getDescription());
        assertEquals(LocalDateTime.of(2026, 9, 10, 18, 0), deadline.getDeadline());

        Event event = assertInstanceOf(Event.class, taskStorage.getTaskEntries().get(2).task());
        assertTrue(event.isDone());
        assertEquals("conference", event.getDescription());
        assertEquals(LocalDateTime.of(2026, 9, 10, 9, 0), event.getStartTime());
        assertEquals(LocalDateTime.of(2026, 9, 11, 17, 0), event.getEndTime());
    }

    @Test
    void loadTaskData_malformedFieldCounts_reportsMalformedStorage() throws Exception {
        Path storagePath = temporaryDirectory.resolve("tasks.csv");
        LocalStorageService storageService = new LocalStorageService(storagePath.toString());
        String[] malformedRecords = {
            "TODO,false,task,extra",
            "DEADLINE,false,task",
            "EVENT,false,task,2026-09-10T09:00"
        };

        for (String malformedRecord : malformedRecords) {
            Files.writeString(storagePath, malformedRecord);

            StorageException exception = assertThrows(
                    StorageException.class, storageService::loadTaskData);
            assertEquals(1, exception.getLineNumber());
        }
    }

    @Test
    void saveTaskData_emptyStorage_writesEmptyFile() throws Exception {
        Path storagePath = temporaryDirectory.resolve("tasks.csv");
        LocalStorageService storageService = new LocalStorageService(storagePath.toString());

        storageService.saveTaskData(new TaskStorage());

        assertEquals("", Files.readString(storagePath));
    }

    @Test
    void saveTaskData_mixedTaskTypes_preservesOrderWithoutTrailingNewline() throws Exception {
        Path storagePath = temporaryDirectory.resolve("tasks.csv");
        LocalStorageService storageService = new LocalStorageService(storagePath.toString());
        TaskStorage taskStorage = new TaskStorage();
        taskStorage.addTask(new Todo("borrow book", true));
        taskStorage.addTask(new Deadline(
                "submit report", false, LocalDateTime.of(2026, 9, 10, 18, 0)));
        taskStorage.addTask(new Event(
                "conference",
                true,
                LocalDateTime.of(2026, 9, 10, 9, 0),
                LocalDateTime.of(2026, 9, 11, 17, 0)));
        String expectedContent = String.join("\n",
                "TODO,true,borrow book",
                "DEADLINE,false,submit report,2026-09-10T18:00",
                "EVENT,true,conference,2026-09-10T09:00,2026-09-11T17:00");

        storageService.saveTaskData(taskStorage);

        assertEquals(expectedContent, Files.readString(storagePath));
        TaskStorage reloadedStorage = storageService.loadTaskData().orElseThrow();
        String reloadedContent = reloadedStorage.getTaskEntries().stream()
                .map(entry -> entry.task())
                .map(Task::encode)
                .collect(Collectors.joining("\n"));
        assertEquals(expectedContent, reloadedContent);
    }
}
