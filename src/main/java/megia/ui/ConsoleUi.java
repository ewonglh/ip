package megia.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

import megia.exception.MegiaException;
import megia.service.LocalizationService;

/**
 * Handles all console input and output for the Megia application.
 */
public final class ConsoleUi implements AutoCloseable {
    private static final String BANNER = """
            /\\ "-./  \\   /\\  ___\\   /\\  ___\\   /\\ \\   /\\  __ \\
            \\ \\ \\-./\\ \\  \\ \\  __\\   \\ \\ \\__‾\\  \\ \\ \\  \\ \\  __ \\
            \\ \\_\\ \\ \\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\  \\ \\_\\ \\_\\
            \\/_/  \\/_/   \\/_____/   \\/_____/   \\/_/   \\/_/\\/_/
            """.stripTrailing() + "\n\n";
    private static final String MESSAGE_SEPARATOR = "\n\033[37m"
            + "─".repeat(80)
            + "\033[0m";

    private final Scanner userInput;
    private final PrintStream standardOutput;
    private final PrintStream errorOutput;

    /**
     * Creates a console UI connected to the system input and output streams.
     */
    public ConsoleUi() {
        this(System.in, System.out, System.err);
    }

    /**
     * Creates a console UI connected to the supplied streams.
     *
     * @param inputStream Stream from which commands are read.
     * @param standardOutput Stream to which normal messages are written.
     * @param errorOutput Stream to which diagnostic messages are written.
     */
    public ConsoleUi(
            InputStream inputStream,
            PrintStream standardOutput,
            PrintStream errorOutput) {
        this.userInput = new Scanner(inputStream);
        this.standardOutput = standardOutput;
        this.errorOutput = errorOutput;
    }

    /**
     * Reads the next command after displaying the command prompt.
     *
     * @return Entered command, or an empty optional when the input stream has ended.
     */
    public Optional<String> readCommand() {
        standardOutput.print("> ");
        if (!userInput.hasNextLine()) {
            return Optional.empty();
        }
        return Optional.of(userInput.nextLine());
    }

    /**
     * Displays the application banner and greeting.
     */
    public void showGreeting() {
        standardOutput.println(MESSAGE_SEPARATOR);
        showMessage(BANNER + LocalizationService.getMessage("greeting"));
    }

    /**
     * Displays a message followed by a console separator.
     *
     * @param message Message to display.
     */
    public void showMessage(String message) {
        standardOutput.println(message);
        standardOutput.println(MESSAGE_SEPARATOR);
    }

    /**
     * Displays a localized message for a recoverable application error.
     *
     * @param exception Error to display.
     */
    public void showError(MegiaException exception) {
        showMessage(LocalizationService.getException(
                exception.getErrorCode(), exception.getMessageArguments()));
    }

    /**
     * Reports an unexpected error and displays a safe message to the user.
     *
     * @param exception Unexpected error to report.
     */
    public void showUnexpectedError(RuntimeException exception) {
        errorOutput.println("Unexpected application error: " + exception.getMessage());
        showMessage(LocalizationService.getMessage("unexpected_error"));
    }

    /**
     * Displays the application farewell.
     */
    public void showFarewell() {
        showMessage(LocalizationService.getMessage("farewell"));
    }

    @Override
    public void close() {
        userInput.close();
    }
}
