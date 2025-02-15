package backend.academy.scrapper.exceptionHandling.exceptions;

public class ScrapperBaseException extends RuntimeException {

    protected String description;

    protected int code = 400;

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public ScrapperBaseException(String message) {
        super(message);
        this.description = message;
    }

    public ScrapperBaseException(String message, int code) {
        super(message);
        this.description = message;
        this.code = code;
    }

    public ScrapperBaseException(String description, String message) {
        super(message);
        this.description = description;
    }

    public ScrapperBaseException(String description, String message, int code) {
        super(message);
        this.description = description;
        this.code = code;
    }

    public ScrapperBaseException(String description, String message, Throwable cause) {
        super(message, cause);
        this.description = description;
    }

    public ScrapperBaseException(String message, Throwable cause) {
        super(message, cause);
        this.description = message;
    }
}
