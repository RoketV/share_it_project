package ru.practicum.shareit.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "different users for one item")
public class DifferentUsersException extends RuntimeException {

    public DifferentUsersException(String message) {
        super(message);
    }
}
