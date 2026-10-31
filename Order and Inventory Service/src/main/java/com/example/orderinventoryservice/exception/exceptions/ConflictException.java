package com.example.orderinventoryservice.exception.exceptions;

import com.example.orderinventoryservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
