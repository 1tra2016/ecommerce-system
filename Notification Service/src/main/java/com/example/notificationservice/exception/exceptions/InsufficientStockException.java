package com.example.notificationservice.exception.exceptions;

import com.example.notificationservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {
    public InsufficientStockException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
