package com.example.userservice.exception.exceptions;

import com.example.userservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends BaseException {
    public InvalidPasswordException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
