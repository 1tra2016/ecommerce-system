package com.example.paymentservice.exception.exceptions;

import com.example.paymentservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ResourceDeletedException extends BaseException {
    public ResourceDeletedException(String message) {
        super(HttpStatus.GONE, message);
    }
}
