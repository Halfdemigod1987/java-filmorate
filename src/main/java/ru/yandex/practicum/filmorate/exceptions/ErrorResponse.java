package ru.yandex.practicum.filmorate.exceptions;

/**
 * @author kazakov
 * @version 16.09.2022
 */
public class ErrorResponse {
    String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
