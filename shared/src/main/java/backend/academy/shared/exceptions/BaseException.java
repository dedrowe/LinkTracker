package backend.academy.shared.exceptions;

public class BaseException extends RuntimeException {

    protected String description;

    protected int code = 400;

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public BaseException(String message) {
        super(message);
        this.description = message;
    }

    public BaseException(String message, int code) {
        super(message);
        this.description = message;
        this.code = code;
    }

    public BaseException(String description, String message) {
        super(message);
        this.description = description;
    }

    public BaseException(String description, String message, int code) {
        super(message);
        this.description = description;
        this.code = code;
    }

    public BaseException(String description, String message, Throwable cause) {
        super(message, cause);
        this.description = description;
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.description = message;
    }
}
