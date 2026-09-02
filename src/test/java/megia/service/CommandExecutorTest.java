package megia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import megia.model.CommandResult;
import megia.model.TaskEntry;
import megia.model.TaskStorage;

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
}
