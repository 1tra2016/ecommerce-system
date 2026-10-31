package com.example.orderinventoryservice.exception.exceptions;

import com.example.orderinventoryservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ResourceDeletedException extends BaseException {
    public ResourceDeletedException(String message) {
        super(HttpStatus.GONE, message);
    }
}
