package backend.academy.scrapper.exceptionHandling.exceptions;

import backend.academy.shared.exceptions.BaseException;
import lombok.Getter;

@Getter
public class LinkException extends BaseException {

    private final String link;

    public LinkException(String message, String link) {
        super(message);
        this.link = link;
    }
}
