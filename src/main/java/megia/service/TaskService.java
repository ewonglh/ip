package megia.service;

import java.time.LocalDate;
import java.util.Optional;

import megia.exception.MegiaException;
import megia.model.CommandResult;
import megia.model.ParsedCommand;
import megia.model.Task;
import megia.model.TaskEntry;
import megia.model.TaskStorage;

/**
 * Applies parsed commands to task data and persists successful mutations.
 */
public final class TaskService {
    private final TaskStorage taskStorage;
    private final LocalStorageService localStorageService;
    private final TaskParser taskParser;

    /**
     * Creates a task service using the supplied task collection and storage.
     *
     * @param taskStorage In-memory task collection to manage.
     * @param localStorageService Storage used to persist mutations.
     */
    public TaskService(TaskStorage taskStorage, LocalStorageService localStorageService) {
        this.taskStorage = taskStorage;
        this.localStorageService = localStorageService;
        this.taskParser = new TaskParser();
    }

    /**
     * Executes one parsed command and returns a structured semantic result.
     *
     * @param command Parsed command to execute.
     * @return Structured command result.
     * @throws MegiaException If the command contains invalid input or storage fails.
     */
    public CommandResult execute(ParsedCommand command) throws MegiaException {
        return switch (command.commandName()) {
            case "list" -> executeList(command);
            case "find" -> executeFind(command);
            case "bye" -> new CommandResult.Exit();
            case "todo", "deadline", "event" -> executeAdd(command);
            case "mark", "unmark", "delete" -> executeTaskMutation(command);
            case "" -> new CommandResult.Empty();
            default -> throw new megia.exception.UserInputException(
                    megia.exception.ErrorCode.UNKNOWN_COMMAND, command.commandName());
        };
    }

    private CommandResult executeList(ParsedCommand command) throws MegiaException {
        Optional<LocalDate> date = taskParser.parseListDate(command);
        if (date.isPresent()) {
            LocalDate requestedDate = date.get();
            return new CommandResult.TaskList(
                    taskStorage.getTaskEntriesOn(requestedDate),
                    new CommandResult.Query(
                            CommandResult.QueryType.DATE, requestedDate.toString()));
        }
        return new CommandResult.TaskList(
                taskStorage.getTaskEntries(),
                new CommandResult.Query(CommandResult.QueryType.ALL, ""));
    }

    private CommandResult executeFind(ParsedCommand command) throws MegiaException {
        String query = taskParser.parseFindQuery(command);
        return new CommandResult.TaskList(
                taskStorage.findTaskEntries(query),
                new CommandResult.Query(CommandResult.QueryType.FIND, query));
    }

    private CommandResult executeAdd(ParsedCommand command) throws MegiaException {
        Task newTask = taskParser.parseNewTask(command);
        taskStorage.addTask(newTask);
        localStorageService.saveTaskData(taskStorage);
        return new CommandResult.TaskMutation(
                CommandResult.MutationType.ADD,
                new TaskEntry(taskStorage.getTaskCount(), newTask),
                taskStorage.getTaskCount());
    }

    private CommandResult executeTaskMutation(ParsedCommand command) throws MegiaException {
        int taskId = taskParser.parseTaskId(command);
        Task task = switch (command.commandName()) {
            case "mark" -> taskStorage.markTask(taskId);
            case "unmark" -> taskStorage.unmarkTask(taskId);
            default -> taskStorage.deleteTask(taskId);
        };
        localStorageService.saveTaskData(taskStorage);
        return new CommandResult.TaskMutation(
                CommandResult.MutationType.valueOf(command.commandName().toUpperCase()),
                new TaskEntry(taskId, task),
                taskStorage.getTaskCount());
    }
}
