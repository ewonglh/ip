package exception;

/**
 * Reports invalid command syntax or values supplied by the user.
 */
public class UserInputException extends MegiaException {
    public UserInputException(ErrorCode errorCode, Object... messageArguments) {
        super(errorCode, messageArguments);
    }
}
