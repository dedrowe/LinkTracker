package backend.academy.scrapper.exceptionHandling.exceptions;

public class NotFoundException extends ScrapperBaseException {

    {
        code = 404;
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String description, String message) {
        super(description, message);
    }

    public NotFoundException(String description, String message, Throwable cause) {
        super(description, message, cause);
    }
}
