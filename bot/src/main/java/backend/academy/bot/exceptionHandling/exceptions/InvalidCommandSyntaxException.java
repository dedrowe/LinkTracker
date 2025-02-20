package backend.academy.bot.exceptionHandling.exceptions;

public class InvalidCommandSyntaxException extends RuntimeException {
    public InvalidCommandSyntaxException(String message) {
        super(message);
    }
}
