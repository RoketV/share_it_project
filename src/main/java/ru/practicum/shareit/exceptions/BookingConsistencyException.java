package ru.practicum.shareit.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "problem with booking consistency")
public class BookingConsistencyException extends RuntimeException {
    public BookingConsistencyException(String message) {
        super(message);
    }
}
