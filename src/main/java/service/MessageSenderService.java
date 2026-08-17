package service;

public class MessageSenderService {

    private static final StringBuilder line = new StringBuilder()
            .append("\n")
            .append("\033[37m")
            .repeat("\u2500", 80)
            .append("\033[0m");

    public static void sendGreeting(String message){
        System.out.println(line);
        MessageSenderService.sendMessage(message);
    }
    public static void sendMessage(String message) {
        System.out.println(message);
        System.out.println(line);
    }
}