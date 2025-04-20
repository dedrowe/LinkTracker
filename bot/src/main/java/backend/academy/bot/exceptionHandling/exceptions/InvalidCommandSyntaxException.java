package backend.academy.bot.exceptionHandling.exceptions;

import lombok.Getter;

@Getter
public class InvalidCommandSyntaxException extends RuntimeException {

    private final String command;

    private final String response;

    public InvalidCommandSyntaxException(String message, String command) {
        super(message);
        this.command = command;
        this.response = message;
    }

    public InvalidCommandSyntaxException(String message, String command, String response) {
        super(message);
        this.command = command;
        this.response = response;
    }
}
