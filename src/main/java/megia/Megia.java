package megia;

import megia.exception.ErrorCode;
import megia.exception.MegiaException;
import megia.exception.UserInputException;
import megia.model.ParsedCommand;
import megia.model.Task;
import megia.model.TaskStorage;
import megia.service.LocalStorageService;
import megia.service.LocalizationService;
import megia.service.PropertiesService;
import megia.service.TaskParser;
import megia.ui.ConsoleUi;

import java.util.Optional;
import java.util.Properties;

/**
 * Coordinates the Megia command-line task manager.
 */
public final class Megia {
    private static final Properties PROPERTIES = PropertiesService.getProperties();

    private final ConsoleUi ui;
    private final LocalStorageService localStorageService;
    private final TaskStorage taskStorage;
    private final TaskParser taskParser;

    /**
     * Creates an application using the supplied UI, local storage, and task collection.
     *
     * @param ui Console interface used to interact with the user.
     * @param localStorageService Storage used to persist tasks.
     * @param taskStorage Task collection managed by the application.
     */
    public Megia(
            ConsoleUi ui,
            LocalStorageService localStorageService,
            TaskStorage taskStorage) {
        this.ui = ui;
        this.localStorageService = localStorageService;
        this.taskStorage = taskStorage;
        this.taskParser = new TaskParser();
    }

    /**
     * Starts the command-line application using the configured storage and system console.
     *
     * @param arguments Command-line arguments, which Megia does not currently use.
     */
    public static void main(String[] arguments) {
        LocalStorageService localStorageService = new LocalStorageService(PROPERTIES.getProperty("storage.task.path"));
        TaskStorage taskStorage = localStorageService.loadTaskData()
                .orElse(new TaskStorage());

        try (ConsoleUi ui = new ConsoleUi()) {
            Megia megia = new Megia(ui, localStorageService, taskStorage);
            megia.run();
        }
    }

    /**
     * Runs the application until the user exits or the input stream closes.
     */
    public void run() {
        boolean shouldExit = false;

        ui.showGreeting();

        while (!shouldExit) {
            try {
                Optional<String> rawInput = ui.readCommand();
                if (rawInput.isEmpty()) {
                    break;
                }

                ParsedCommand command = taskParser.parseCommand(rawInput.get());

                switch (command.commandName()) {
                    case "list" -> ui.showMessage(taskStorage.toString());
                    case "bye" -> shouldExit = true;
                    case "todo", "deadline", "event" -> {
                        Task newTask = taskParser.parseNewTask(command);
                        taskStorage.addTask(newTask);

                        ui.showMessage(
                                LocalizationService.getMessage("task_storage_add")
                                        + "\n"
                                        + "  " + newTask
                                        + "\n"
                                        + String.format(
                                                LocalizationService.getMessage("task_storage_add_2"),
                                                taskStorage.getTaskCount()));
                        localStorageService.saveTaskData(taskStorage);
                    }
                    case "mark", "unmark", "delete" -> {
                        int taskId = taskParser.parseTaskId(command);
                        Task task = switch (command.commandName()) {
                            case "mark" -> taskStorage.markTask(taskId);
                            case "unmark" -> taskStorage.unmarkTask(taskId);
                            default -> taskStorage.deleteTask(taskId);
                        };
                        String messageKey = "task_storage_" + command.commandName();

                        ui.showMessage(
                                LocalizationService.getMessage(messageKey) + "\n" + task);
                        localStorageService.saveTaskData(taskStorage);
                    }
                    case "" -> ui.showMessage(
                            LocalizationService.getMessage("empty"));
                    default -> throw new UserInputException(ErrorCode.UNKNOWN_COMMAND, command.commandName());
                }
            } catch (MegiaException exception) {
                ui.showError(exception);
            } catch (RuntimeException exception) {
                ui.showUnexpectedError(exception);
            }
        }

        ui.showFarewell();
        localStorageService.saveTaskData(taskStorage);
    }
}