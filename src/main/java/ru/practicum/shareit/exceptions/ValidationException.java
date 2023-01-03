package ru.practicum.shareit.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "validation problems")
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
