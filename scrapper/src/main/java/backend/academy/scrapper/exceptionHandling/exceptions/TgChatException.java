package backend.academy.scrapper.exceptionHandling.exceptions;

import backend.academy.shared.exceptions.BaseException;
import lombok.Getter;

@Getter
public class TgChatException extends BaseException {

    private final String id;

    public TgChatException(String message, String id) {
        super(message);
        this.id = id;
    }
}
