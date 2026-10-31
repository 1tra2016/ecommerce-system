package com.example.productservice.exception.exceptions;

import com.example.productservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}