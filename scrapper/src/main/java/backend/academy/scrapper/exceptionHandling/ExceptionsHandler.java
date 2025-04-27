package backend.academy.scrapper.exceptionHandling;

import backend.academy.scrapper.exceptionHandling.exceptions.LinkDataException;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
import backend.academy.scrapper.exceptionHandling.exceptions.TgChatException;
import backend.academy.scrapper.exceptionHandling.exceptions.WrongServiceException;
import backend.academy.shared.dto.ApiErrorResponse;
import backend.academy.shared.exceptions.ApiCallException;
import backend.academy.shared.exceptions.BaseException;
import java.util.Arrays;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionsHandler {

    private static final String LOG_MESSAGE = "Произошла ошибка";

    @ExceptionHandler(TgChatException.class)
    public ResponseEntity<ApiErrorResponse> handle(TgChatException ex) {
        try (var ignored = MDC.putCloseable("id", ex.id())) {
            log.error(LOG_MESSAGE, ex);
        }
        return new ResponseEntity<>(createResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LinkException.class)
    public ResponseEntity<ApiErrorResponse> handle(LinkException ex) {
        try (var ignored = MDC.putCloseable("link", ex.link())) {
            log.error(LOG_MESSAGE, ex);
        }
        return new ResponseEntity<>(createResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LinkDataException.class)
    public ResponseEntity<ApiErrorResponse> handle(LinkDataException ex) {
        try (var ignored = MDC.putCloseable("linkId", ex.linkId());
                var ignored1 = MDC.putCloseable("chatId", ex.chatId())) {
            log.error(LOG_MESSAGE, ex);
        }
        return new ResponseEntity<>(createResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongServiceException.class)
    public ResponseEntity<ApiErrorResponse> handle(WrongServiceException ex) {
        try (var ignored = MDC.putCloseable("service", ex.service())) {
            log.error(LOG_MESSAGE, ex);
        }
        return new ResponseEntity<>(createResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiCallException.class)
    public ResponseEntity<ApiErrorResponse> handle(ApiCallException ex) {
        try (var ignored = MDC.putCloseable("url", ex.url())) {
            log.error(LOG_MESSAGE, ex);
        }
        return new ResponseEntity<>(createResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorResponse> handle(BaseException ex) {
        log.error(LOG_MESSAGE, ex);
        return new ResponseEntity<>(createResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiErrorResponse> handle(CallNotPermittedException ex) {
        log.error(LOG_MESSAGE, ex);
        ApiErrorResponse errorResponse = new ApiErrorResponse(
            "Этот сервис временно недоступен, попробуйте позже",
            HttpStatus.valueOf(500),
            ex.getClass().getName(),
            ex.getMessage(),
            Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handle(Exception ex) {
        log.error(LOG_MESSAGE, ex);
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Некорректные параметры запроса",
                HttpStatus.valueOf(500),
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private ApiErrorResponse createResponse(BaseException ex) {
        return new ApiErrorResponse(
                ex.description(),
                HttpStatus.resolve(ex.code()),
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
    }
}
