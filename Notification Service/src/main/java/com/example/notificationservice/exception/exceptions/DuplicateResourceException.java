package com.example.notificationservice.exception.exceptions;

import com.example.notificationservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
