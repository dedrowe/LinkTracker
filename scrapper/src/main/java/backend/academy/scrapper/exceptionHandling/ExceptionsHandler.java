package backend.academy.scrapper.exceptionHandling;

import backend.academy.scrapper.exceptionHandling.exceptions.NotFoundException;
import backend.academy.shared.dto.ApiErrorResponse;
import backend.academy.shared.exceptions.ApiCallException;
import backend.academy.shared.exceptions.BaseException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handle(NotFoundException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                ex.getDescription(),
                HttpStatus.resolve(ex.getCode()),
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handle(ApiCallException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                ex.getDescription(),
                HttpStatus.resolve(ex.getCode()),
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handle(BaseException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                ex.getDescription(),
                HttpStatus.resolve(ex.getCode()),
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handle(Exception ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                "Некорректные параметры запроса",
                HttpStatus.valueOf("500"),
                ex.getClass().getName(),
                ex.getMessage(),
                Arrays.stream(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
