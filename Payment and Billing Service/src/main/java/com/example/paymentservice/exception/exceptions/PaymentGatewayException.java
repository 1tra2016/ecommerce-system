package com.example.paymentservice.exception.exceptions;

import com.example.paymentservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PaymentGatewayException extends BaseException {
    public PaymentGatewayException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR,message);
    }
}
