package backend.academy.shared.exceptions;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    protected String description;

    protected int code = 400;

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

    public BaseException(String description, String message, int code, Throwable cause) {
        super(message, cause);
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
