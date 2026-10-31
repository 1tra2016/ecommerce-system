package com.example.paymentservice.dto.vnpay;

public record VnpayIpnResponse(
        String RspCode,
        String Message
) {}