package com.example.productservice.exception.exceptions;

import com.example.productservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ResourceDeletedException extends BaseException {
    public ResourceDeletedException(String message) {
        super(HttpStatus.GONE, message);
    }
}
