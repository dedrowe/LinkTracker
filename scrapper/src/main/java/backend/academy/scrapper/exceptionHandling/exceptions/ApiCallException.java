package backend.academy.scrapper.exceptionHandling.exceptions;

public class ApiCallException extends ScrapperBaseException {

    public ApiCallException(String message, int code) {
        super(message, code);
    }

    public ApiCallException(String description, String message, int code) {
        super(description, message, code);
    }
}
