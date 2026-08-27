package megia.service;

/**
 * Sends consistently separated messages to the console.
 */
public final class MessageSenderService {
    private static final String MESSAGE_SEPARATOR = "\n\033[37m"
            + "─".repeat(80)
            + "\033[0m";

    private MessageSenderService() {
    }

    /**
     * Sends the initial greeting between console separators.
     *
     * @param message Greeting text to send.
     */
    public static void sendGreeting(String message) {
        System.out.println(MESSAGE_SEPARATOR);
        sendMessage(message);
    }

    /**
     * Sends a message followed by a console separator.
     *
     * @param message Message text to send.
     */
    public static void sendMessage(String message) {
        System.out.println(message);
        System.out.println(MESSAGE_SEPARATOR);
    }
}
