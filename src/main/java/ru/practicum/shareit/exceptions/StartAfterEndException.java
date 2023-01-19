package ru.practicum.shareit.exceptions;

public class StartAfterEndException extends RuntimeException {

    public StartAfterEndException(String message) {
        super(message);
    }
}
