public class MessageSender {

    private static final StringBuilder line = new StringBuilder()
            .append("\n")
            .append("\033[37m")
            .repeat("\u2500", 80)
            .append("\033[0m");

    public static void sendGreeting(String message){
        System.out.println(line);
        MessageSender.sendMessage(message);
    }
    public static void sendMessage(String message) {
        System.out.println(message);
        System.out.println(line);
    }
}