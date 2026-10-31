package com.example.userservice.exception.exceptions;

import com.example.userservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidFileException extends BaseException {
    public InvalidFileException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}