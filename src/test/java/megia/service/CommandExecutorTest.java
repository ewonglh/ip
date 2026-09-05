package megia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import megia.exception.StorageException;
import megia.model.CommandResult;
import megia.model.Deadline;
import megia.model.Event;
import megia.model.TaskEntry;
import megia.model.TaskStorage;
import megia.model.Todo;

class CommandExecutorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void execute_addThenList_preservesOriginalTaskId() throws Exception {
        TaskStorage taskStorage = new TaskStorage();
        LocalStorageService localStorageService = new LocalStorageService(
                temporaryDirectory.resolve("tasks.csv").toString());
        CommandExecutor commandExecutor = new CommandExecutor(
                new TaskService(taskStorage, localStorageService));

        CommandResult addResult = commandExecutor.execute("todo borrow book");
        CommandResult.TaskMutation addMutation = assertInstanceOf(
                CommandResult.TaskMutation.class, addResult);
        assertEquals(1, addMutation.task().id());

        CommandResult listResult = commandExecutor.execute("list");
        CommandResult.TaskList taskList = assertInstanceOf(CommandResult.TaskList.class, listResult);
        TaskEntry taskEntry = taskList.entries().get(0);
        assertEquals(1, taskEntry.id());
        assertEquals("borrow book", taskEntry.task().getDescription());
    }

    @Test
    void execute_findAndDateList_returnsMatchingTasksWithOriginalIds() throws Exception {
        TaskStorage taskStorage = new TaskStorage();
        LocalStorageService localStorageService = new LocalStorageService(
                temporaryDirectory.resolve("tasks.csv").toString());
        CommandExecutor commandExecutor = new CommandExecutor(
                new TaskService(taskStorage, localStorageService));

        commandExecutor.execute("todo read book");
        commandExecutor.execute("deadline submit report /by 2026-09-03 1800");
        commandExecutor.execute("event conference /from 2026-09-02 1400 /to 2026-09-04 1600");

        CommandResult.TaskList findResult = assertInstanceOf(
                CommandResult.TaskList.class, commandExecutor.execute("find book"));
        assertEquals(CommandResult.QueryType.FIND, findResult.query().type());
        assertEquals(1, findResult.entries().size());
        assertEquals(1, findResult.entries().get(0).id());

        CommandResult.TaskList dateResult = assertInstanceOf(
                CommandResult.TaskList.class,
                commandExecutor.execute("list " + LocalDate.of(2026, 9, 3)));
        assertEquals(CommandResult.QueryType.DATE, dateResult.query().type());
        assertEquals(2, dateResult.entries().size());
        assertEquals(2, dateResult.entries().get(0).id());
        assertEquals(Deadline.class, dateResult.entries().get(0).task().getClass());
        assertEquals(3, dateResult.entries().get(1).id());
        assertEquals(Event.class, dateResult.entries().get(1).task().getClass());
    }

    @Test
    void execute_mutations_persistAcrossServiceRestart() throws Exception {
        Path storagePath = temporaryDirectory.resolve("tasks.csv");
        LocalStorageService localStorageService = new LocalStorageService(storagePath.toString());
        TaskStorage taskStorage = new TaskStorage();
        CommandExecutor commandExecutor = new CommandExecutor(
                new TaskService(taskStorage, localStorageService));

        commandExecutor.execute("todo keep this task");
        commandExecutor.execute("todo remove this task");
        commandExecutor.execute("mark 1");
        commandExecutor.execute("delete 2");

        TaskStorage reloadedStorage = localStorageService.loadTaskData().orElseThrow();
        assertEquals(1, reloadedStorage.getTaskCount());
        assertTrue(reloadedStorage.getTaskEntries().get(0).task().isDone());
        assertEquals("keep this task", reloadedStorage.getTaskEntries().get(0).task().getDescription());
    }

    @Test
    void execute_failedPersistence_rollsBackInMemoryMutation() throws Exception {
        Path storagePath = temporaryDirectory.resolve("missing-parent").resolve("tasks.csv");
        LocalStorageService localStorageService = new LocalStorageService(storagePath.toString());
        TaskStorage taskStorage = new TaskStorage();
        taskStorage.addTask(new Todo("do not change"));
        CommandExecutor commandExecutor = new CommandExecutor(
                new TaskService(taskStorage, localStorageService));

        assertThrows(StorageException.class, () -> commandExecutor.execute("mark 1"));
        assertFalse(taskStorage.getTaskEntries().get(0).task().isDone());

        assertThrows(StorageException.class, () -> commandExecutor.execute("delete 1"));
        assertEquals(1, taskStorage.getTaskCount());
        assertEquals("do not change", taskStorage.getTaskEntries().get(0).task().getDescription());

        assertThrows(StorageException.class, () -> commandExecutor.execute("todo another task"));
        assertEquals(1, taskStorage.getTaskCount());
    }
}
