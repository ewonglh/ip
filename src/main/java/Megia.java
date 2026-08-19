import model.Task;
import model.TaskStorage;
import service.LocalizationService;
import service.MessageSenderService;
import service.PropertiesService;
import service.TaskService;

public static String banner = """
    /\\ "-./  \\   /\\  ___\\   /\\  ___\\   /\\ \\   /\\  __ \\
    \\ \\ \\-./\\ \\  \\ \\  __\\   \\ \\ \\__‾\\  \\ \\ \\  \\ \\  __ \\
    \\ \\_\\ \\ \\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\  \\ \\_\\ \\_\\
    \\/_/  \\/_/   \\/_____/   \\/_____/   \\/_/   \\/_/\\/_/
    """.stripTrailing() + "\n\n";

void main() {
  Properties prop = PropertiesService.getProperties();
  TaskStorage taskStorage = new TaskStorage();
  Scanner userInput = new Scanner(System.in);
  String cmd, rawInput, body;
  boolean end = false;

  MessageSenderService.sendGreeting(banner +
      LocalizationService.getMessage("greeting"));

  while (!end) {
    IO.print("> ");
    rawInput = userInput.nextLine();
    cmd = Arrays.stream(rawInput.split(" ")).findFirst().orElse("");
    body = Arrays.stream(rawInput.split(" ")).skip(1).collect(Collectors.joining(" "));

    switch (cmd) {
      case "list" -> MessageSenderService.sendMessage(taskStorage.toString());
      case "bye" -> end = true;
      case "todo", "deadline", "event" -> TaskService.parseTaskAddition(cmd, body, taskStorage);
      case "mark", "unmark" -> TaskService.handleTaskStatusCommand(cmd, body, taskStorage);
      case "" -> MessageSenderService.sendMessage(LocalizationService.getMessage("empty"));
      default -> MessageSenderService.sendMessage(LocalizationService.getMessage("invalid_command"));
    }
  }
  userInput.close();
  MessageSenderService.sendMessage(LocalizationService.getMessage("farewell"));
}