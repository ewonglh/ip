package megia.exception;

/**
 * Reports invalid commandName syntax or values supplied by the user.
 */
public class UserInputException extends MegiaException {
    /**
     * Creates an error for invalid commandName syntax or values.
     *
     * @param errorCode Identifies the message to display.
     * @param messageArguments Values interpolated into the message.
     */
    public UserInputException(ErrorCode errorCode, Object... messageArguments) {
        super(errorCode, messageArguments);
    }
}