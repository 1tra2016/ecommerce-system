package com.example.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // HS256 cần khóa đủ mạnh: tối thiểu 256 bit.
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 bytes for HS256"
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims validateToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Không chấp nhận token thiếu thời hạn hết hiệu lực.
        Date expiration = claims.getExpiration();

        if (expiration == null || !expiration.after(new Date())) {
            throw new JwtException(
                    "JWT is expired or has no expiration claim"
            );
        }

        if (claims.getSubject() == null
                || claims.getSubject().isBlank()) {
            throw new JwtException("JWT subject is missing");
        }

        return claims;
    }
}
