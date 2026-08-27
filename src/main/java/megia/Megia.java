package megia;

import megia.exception.ErrorCode;
import megia.exception.MegiaException;
import megia.exception.UserInputException;
import megia.model.Task;
import megia.model.TaskStorage;
import megia.service.*;

import java.util.Properties;
import java.util.Scanner;

/**
 * Starts the Megia command-line task manager and handles its console interaction loop.
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
     * Runs the command-line application until the user exits or the input stream closes.
     *
     * @param arguments Command-line arguments, which Megia does not currently use.
     */
    public static void main(String[] arguments) {
        LocalStorageService localStorageService = new LocalStorageService(PROPERTIES.getProperty("storage.task.path"));
        TaskStorage taskStorage = localStorageService.loadTaskData()
                .orElse(new TaskStorage());
        TaskService taskService = new TaskService(taskStorage);
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
                String trimmedInput = rawInput.strip();
                String command;
                String body;
                if (trimmedInput.isEmpty()) {
                    command = "";
                    body = "";
                } else {
                    String[] inputParts = trimmedInput.split("\\s+", 2);
                    command = inputParts[0];
                    body = inputParts.length > 1 ? inputParts[1] : "";
                }

                switch (command) {
                    case "list" -> MessageSenderService.sendMessage(taskStorage.toString());
                    case "bye" -> shouldExit = true;
                    case "todo", "deadline", "event" -> {
                        Task newTask = taskParser.parse(command, body);
                        taskService.addTask(newTask);

                        MessageSenderService.sendMessage(
                                LocalizationService.getMessage("task_storage_add")
                                        + "\n"
                                        + "  " + newTask
                                        + "\n"
                                        + String.format(
                                                LocalizationService.getMessage("task_storage_add_2"),
                                                taskService.getTaskCount()));
                        localStorageService.saveTaskData(taskStorage);
                    }
                    case "mark", "unmark", "delete" -> {
                        int taskId = taskParser.parseTaskId(body, command);
                        Task task = switch (command) {
                            case "mark" -> taskService.markTask(taskId);
                            case "unmark" -> taskService.unmarkTask(taskId);
                            default -> taskService.deleteTask(taskId);
                        };
                        String messageKey = "task_storage_" + command;

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