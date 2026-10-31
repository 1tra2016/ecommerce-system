package com.example.orderinventoryservice.exception.exceptions;

import com.example.orderinventoryservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidFileException extends BaseException {
    public InvalidFileException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
