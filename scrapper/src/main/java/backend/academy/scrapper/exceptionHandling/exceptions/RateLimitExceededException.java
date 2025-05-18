package backend.academy.scrapper.exceptionHandling.exceptions;

import backend.academy.shared.exceptions.BaseException;

public class RateLimitExceededException extends BaseException {

    public RateLimitExceededException(String message, int code) {
        super(message, code);
    }
}
