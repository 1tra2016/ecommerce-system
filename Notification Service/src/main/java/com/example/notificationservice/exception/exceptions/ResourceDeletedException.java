package com.example.notificationservice.exception.exceptions;

import com.example.notificationservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ResourceDeletedException extends BaseException {
    public ResourceDeletedException(String message) {
        super(HttpStatus.GONE, message);
    }
}
