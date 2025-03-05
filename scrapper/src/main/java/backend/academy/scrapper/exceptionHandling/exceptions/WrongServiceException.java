package backend.academy.scrapper.exceptionHandling.exceptions;

import backend.academy.shared.exceptions.BaseException;
import lombok.Getter;

@Getter
public class WrongServiceException extends BaseException {

    private final String service;

    public WrongServiceException(String message, String service) {
        super(message);
        this.service = service;
    }

    public WrongServiceException(String message, String service, Throwable cause) {
        super(message, cause);
        this.service = service;
    }
}
