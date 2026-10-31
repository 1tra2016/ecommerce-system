package com.example.paymentservice.exception.exceptions;

import com.example.paymentservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {
    public InsufficientStockException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
