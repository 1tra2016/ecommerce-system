package com.example.productservice.exception.exceptions;

import com.example.productservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}