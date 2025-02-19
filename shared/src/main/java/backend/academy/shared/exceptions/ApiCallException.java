package backend.academy.shared.exceptions;

public class ApiCallException extends BaseException {

    public ApiCallException(String message, int code) {
        super(message, code);
    }

    public ApiCallException(String description, String message, int code) {
        super(description, message, code);
    }
}
