package backend.academy.bot.exceptionHandling.exceptions;

import lombok.Getter;

@Getter
public class InvalidCommandSyntaxException extends RuntimeException {

    private final String command;

    public InvalidCommandSyntaxException(String message, String command) {
        super(message);
        this.command = command;
    }
}
