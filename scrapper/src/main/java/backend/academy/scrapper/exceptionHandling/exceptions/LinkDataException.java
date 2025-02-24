package backend.academy.scrapper.exceptionHandling.exceptions;

import backend.academy.shared.exceptions.BaseException;
import lombok.Getter;

@Getter
public class LinkDataException extends BaseException {

    protected final String linkId;

    protected final String chatId;

    public LinkDataException(String message, String linkId, String chatId) {
        super(message);
        this.linkId = linkId;
        this.chatId = chatId;
    }
}
