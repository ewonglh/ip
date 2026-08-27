package megia;

import megia.exception.ErrorCode;
import megia.exception.MegiaException;
import megia.exception.UserInputException;
import megia.model.ParsedCommand;
import megia.model.Task;
import megia.model.TaskStorage;
import megia.service.*;

import java.util.Properties;
import java.util.Scanner;

/**
 * Starts the Megia commandName-line task manager and handles its console interaction loop.
 */
public final class Megia {
    private static final Properties PROPERTIES = PropertiesService.getProperties();
    private static final String BANNER = """
            /\\ "-./  \\   /\\  ___\\   /\\  ___\\   /\\ \\   /\\  __ \\
            \\ \\ \\-./\\ \\  \\ \\  __\\   \\ \\ \\__‾\\  \\ \\ \\  \\ \\  __ \\
            \\ \\_\\ \\ \\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\  \\ \\_\\ \\_\\
            \\/_/  \\/_/   \\/_____/   \\/_____/   \\/_/   \\/_/\\/_/
            """.stripTrailing() + "\n\n";

    private Megia() {
    }

    /**
     * Runs the commandName-line application until the user exits or the input stream closes.
     *
     * @param arguments Command-line arguments, which Megia does not currently use.
     */
    public static void main(String[] arguments) {
        LocalStorageService localStorageService = new LocalStorageService(PROPERTIES.getProperty("storage.task.path"));
        TaskStorage taskStorage = localStorageService.loadTaskData()
                .orElse(new TaskStorage());
        TaskParser taskParser = new TaskParser();
        Scanner userInput = new Scanner(System.in);
        boolean shouldExit = false;

        MessageSenderService.sendGreeting(BANNER
                + LocalizationService.getMessage("greeting"));

        while (!shouldExit) {
            try {
                System.out.print("> ");
                if (!userInput.hasNextLine()) {
                    break;
                }

                String rawInput = userInput.nextLine();
                ParsedCommand command = taskParser.parseCommand(rawInput);

                switch (command.commandName()) {
                    case "list" -> MessageSenderService.sendMessage(taskStorage.toString());
                    case "bye" -> shouldExit = true;
                    case "todo", "deadline", "event" -> {
                        Task newTask = taskParser.parseNewTask(command);
                        taskStorage.addTask(newTask);

                        MessageSenderService.sendMessage(
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

                        MessageSenderService.sendMessage(
                                LocalizationService.getMessage(messageKey) + "\n" + task);
                        localStorageService.saveTaskData(taskStorage);
                    }
                    case "" -> MessageSenderService.sendMessage(
                            LocalizationService.getMessage("empty"));
                    default -> throw new UserInputException(ErrorCode.UNKNOWN_COMMAND, command);
                }
            } catch (MegiaException exception) {
                MessageSenderService.sendMessage(LocalizationService.getException(
                        exception.getErrorCode(), exception.getMessageArguments()));
            } catch (RuntimeException exception) {
                System.err.println("Unexpected application error: " + exception.getMessage());
                MessageSenderService.sendMessage(
                        LocalizationService.getMessage("unexpected_error"));
            }
        }

        userInput.close();
        MessageSenderService.sendMessage(LocalizationService.getMessage("farewell"));
        localStorageService.saveTaskData(taskStorage);
    }
}