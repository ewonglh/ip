import data.Task;
import data.TaskStorage;
import service.LocalizationService;
import service.MessageSenderService;
import service.PropertiesService;

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
            case "mark", "unmark" -> handleTaskStatusCommand(cmd, body, taskStorage);
            case "" -> MessageSenderService.sendMessage(LocalizationService.getMessage("empty"));
            default -> {
                taskStorage.add(new Task(rawInput));
                MessageSenderService.sendMessage(LocalizationService.getMessage("task_storage_add")
                        + " " + rawInput);
            }
        }
    }
    MessageSenderService.sendMessage(LocalizationService.getMessage("farewell"));
}

void handleTaskStatusCommand(String command, String body, TaskStorage taskStorage) {
    final int taskId;

    try {
        taskId = Integer.parseInt(body.trim());
    } catch (NumberFormatException e) {
        MessageSenderService.sendMessage(LocalizationService.getMessage("task_invalid_id"));
        return;
    }

    if (!taskStorage.isValidTaskId(taskId)) {
        MessageSenderService.sendMessage(LocalizationService.getMessage("task_invalid_id"));
        return;
    }

    String messageKey;
    if (command.equals("mark")) {
        taskStorage.markAsDone(taskId);
        messageKey = "task_storage_mark";
    } else {
        taskStorage.markAsNotDone(taskId);
        messageKey = "task_storage_unmark";
    }

    MessageSenderService.sendMessage(
            LocalizationService.getMessage(messageKey) + "\n" + taskStorage.getTask(taskId));
}