package backend.academy.shared.exceptions;

public class NotRetryApiCallException extends ApiCallException {

    public NotRetryApiCallException(String message, int code, String url) {
        super(message, code, url);
    }

    public NotRetryApiCallException(String description, String message, int code, String url) {
        super(description, message, code, url);
    }

    public NotRetryApiCallException(ApiCallException cause) {
        super(cause.description(), cause.getMessage(), cause.code(), cause.url(), cause);
    }
}
