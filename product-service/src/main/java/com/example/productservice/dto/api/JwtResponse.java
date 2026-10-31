package com.example.productservice.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String accessToken;
    private String type;
    private String username;
}