
package com.example.orderinventoryservice.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid OrderItemRequest> items,

        @NotBlank(message = "Shipping address must not be blank")
        String shippingAddress,

        @NotBlank(message = "Email address must not be blank")
        @Email
        String emailAddress
) {
}
