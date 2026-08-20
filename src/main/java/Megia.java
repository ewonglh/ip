import exception.ErrorCode;
import exception.MegiaException;
import exception.UserInputException;
import model.Task;
import model.TaskStorage;
import service.LocalizationService;
import service.MessageSenderService;
import service.TaskParser;
import service.TaskService;

public static String banner = """
    /\\ "-./  \\   /\\  ___\\   /\\  ___\\   /\\ \\   /\\  __ \\
    \\ \\ \\-./\\ \\  \\ \\  __\\   \\ \\ \\__‾\\  \\ \\ \\  \\ \\  __ \\
    \\ \\_\\ \\ \\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\  \\ \\_\\ \\_\\
    \\/_/  \\/_/   \\/_____/   \\/_____/   \\/_/   \\/_/\\/_/
    """.stripTrailing() + "\n\n";

void main() {
  TaskStorage taskStorage = new TaskStorage();
  TaskService taskService = new TaskService(taskStorage);
  TaskParser taskParser = new TaskParser();
  Scanner userInput = new Scanner(System.in);
  String cmd, rawInput, body;
  boolean end = false;

  MessageSenderService.sendGreeting(banner +
      LocalizationService.getMessage("greeting"));

  while (!end) {
      try {
          IO.print("> ");
          if (!userInput.hasNextLine()) {
              break;
          }
          rawInput = userInput.nextLine();
          String trimmedInput = rawInput.strip();
          if (trimmedInput.isEmpty()) {
              cmd = "";
              body = "";
          } else {
              String[] inputParts = trimmedInput.split("\\s+", 2);
              cmd = inputParts[0];
              body = inputParts.length > 1 ? inputParts[1] : "";
          }

          switch (cmd) {
              case "list" -> MessageSenderService.sendMessage(taskStorage.toString());
              case "bye" -> end = true;
              case "todo", "deadline", "event" -> {
                  Task newTask = taskParser.parse(cmd, body);
                  taskService.addTask(newTask);

                  MessageSenderService.sendMessage(
                          LocalizationService.getMessage("task_storage_add") +
                                  "\n" +
                                  "  " + newTask +
                                  "\n" +
                                  String.format(
                                          LocalizationService.getMessage("task_storage_add_2"),
                                          taskService.getTaskCount()));
              }
              case "mark", "unmark", "delete" -> {
                  int taskId = taskParser.parseTaskId(body, cmd);
                  Task task = cmd.equals("mark")
                          ? taskService.markTask(taskId)
                          : cmd.equals("unmark")
                          ? taskService.unmarkTask(taskId)
                          : taskService.deleteTask(taskId);
                  String messageKey = "task_storage_" + cmd;

                  MessageSenderService.sendMessage(
                          LocalizationService.getMessage(messageKey) + "\n" + task);
              }
              case "" -> MessageSenderService.sendMessage(LocalizationService.getMessage("empty"));
              default -> throw new UserInputException(ErrorCode.UNKNOWN_COMMAND, cmd);
          }
      } catch (MegiaException e) {
          MessageSenderService.sendMessage(LocalizationService.getException(
                  e.getErrorCode(), e.getMessageArguments()));
      } catch (RuntimeException e) {
          System.err.println("Unexpected application error: " + e.getMessage());
          MessageSenderService.sendMessage(LocalizationService.getMessage("unexpected_error"));
      }
  }
  userInput.close();
  MessageSenderService.sendMessage(LocalizationService.getMessage("farewell"));
}