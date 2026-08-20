package exception;

import service.LocalizationService;

public class UserInputException extends MegiaException {
    public UserInputException(ErrorCode error) {
        super(LocalizationService.getException(String.valueOf(error)));
    }
}