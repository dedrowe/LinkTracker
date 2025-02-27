package backend.academy.bot.exceptionHandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionsHandler {

    private static final String LOG_MESSAGE = "Произошла ошибка";

    @ExceptionHandler(Throwable.class)
    public void handleException(Throwable e) {
        log.error(LOG_MESSAGE, e);
    }
}
