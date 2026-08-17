import service.LocalizationService;
import service.MessageSenderService;
import service.PropertiesService;

import java.util.*;

public class Megia {

    public static String banner = """
                /\\ "-./  \\   /\\  ___\\   /\\  ___\\   /\\ \\   /\\  __ \\
                \\ \\ \\-./\\ \\  \\ \\  __\\   \\ \\ \\__‾\\  \\ \\ \\  \\ \\  __ \\
                \\ \\_\\ \\ \\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\  \\ \\_\\ \\_\\
                \\/_/  \\/_/   \\/_____/   \\/_____/   \\/_/   \\/_/\\/_/
                """.stripTrailing() + "\n\n";

    public static void main(String[] args) {
        Properties prop = PropertiesService.getProperties();
        Storage<String> storage = new Storage<>();
        Scanner userInput = new Scanner(System.in);
        String cmd = "";
        String rawInput = "";
        boolean end = false;

        MessageSenderService.sendGreeting(banner +
                LocalizationService.getMessage("greeting"));

        while (!end) {
            System.out.print("> ");
            rawInput = userInput.nextLine();
            cmd = Arrays.stream(rawInput.split(" ")).toList().getFirst().toLowerCase();

            switch (cmd) {
                case "list" ->
                    MessageSenderService.sendMessage(storage.toString());
                case "bye" ->
                    end = true;
                default -> {
                    storage.add(rawInput);
                    MessageSenderService.sendMessage(LocalizationService.getMessage("storage_add")
                            + " " + rawInput);
                }
            }
        }
        MessageSenderService.sendMessage(LocalizationService.getMessage("farewell"));
    }
}