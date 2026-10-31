package com.example.paymentservice.exception.exceptions;

import com.example.paymentservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends BaseException {
    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
