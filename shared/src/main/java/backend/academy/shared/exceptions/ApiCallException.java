package backend.academy.shared.exceptions;

import lombok.Getter;

@Getter
public class ApiCallException extends BaseException {

    private final String url;

    public ApiCallException(String message, int code, String url) {
        this(message, message, code, url);
    }

    public ApiCallException(String description, String message, int code, String url) {
        super(description, message, code);
        this.url = url;
    }
}
