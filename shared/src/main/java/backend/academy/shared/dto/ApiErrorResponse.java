package backend.academy.shared.dto;

import java.util.List;
import org.springframework.http.HttpStatus;

public record ApiErrorResponse(
        String description, HttpStatus code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
