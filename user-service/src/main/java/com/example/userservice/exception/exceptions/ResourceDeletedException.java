package com.example.userservice.exception.exceptions;

import com.example.userservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ResourceDeletedException extends BaseException {
    public ResourceDeletedException(String message) {
        super(HttpStatus.GONE, message);
    }
}
