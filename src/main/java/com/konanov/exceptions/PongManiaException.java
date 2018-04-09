package com.konanov.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PongManiaException extends RuntimeException {
    public PongManiaException(String message) { super(message); }
}
