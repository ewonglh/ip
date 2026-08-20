package exception;

import java.util.Objects;

/**
 * Base class for recoverable errors that can be shown to the user.
 * The exception retains structured error information so localization remains
 * the responsibility of the console interface.
 */
public abstract class MegiaException extends Exception {
    private final ErrorCode errorCode;
    private final Object[] messageArguments;

    /**
     * Creates an exception with a localized-message key and formatting values.
     *
     * @param errorCode identifies the message to display
     * @param messageArguments values interpolated into the message
     */
    protected MegiaException(ErrorCode errorCode, Object... messageArguments) {
        super(Objects.requireNonNull(errorCode).name());
        this.errorCode = errorCode;
        this.messageArguments = messageArguments.clone();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns a defensive copy of the values used to format the message.
     */
    public Object[] getMessageArguments() {
        return messageArguments.clone();
    }
}
