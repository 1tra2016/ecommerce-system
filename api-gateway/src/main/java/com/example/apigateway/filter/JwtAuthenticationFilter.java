
package com.example.apigateway.filter;

import com.example.apigateway.exception.GatewayErrorWriter;
import com.example.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final Set<String> ALLOWED_ROLES =
            Set.of("USER", "ADMIN");

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final JwtUtil jwtUtil;
    private final GatewayErrorWriter errorWriter;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        String path = exchange.getRequest()
                .getURI()
                .getPath();

        // Endpoint công khai: không yêu cầu access token.
        if (isPublicAuthPath(path)) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (auth == null
                || !auth.startsWith("Bearer ")
                || auth.substring(7).isBlank()) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = auth.substring(7).trim();

        try {
            Claims claims = jwtUtil.validateToken(token);

            String role = claims.get("role", String.class);
            String subject = claims.getSubject();

            if (role == null
                    || !ALLOWED_ROLES.contains(role)
                    || subject == null
                    || subject.isBlank()) {
                return reject(exchange, HttpStatus.UNAUTHORIZED);
            }

            // Phân quyền theo đường dẫn tại Gateway.
            if (isAdminPath(path) && !"ADMIN".equals(role)) {
                return reject(exchange, HttpStatus.FORBIDDEN);
            }

            // Không tin header định danh do client gửi lên.
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove(USER_ID_HEADER);
                        headers.remove(USER_ROLE_HEADER);

                        headers.set(USER_ID_HEADER, subject);
                        headers.set(USER_ROLE_HEADER, role);
                    })
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(request)
                    .build();

            return chain.filter(mutatedExchange);

        } catch (JwtException | IllegalArgumentException e) {
            log.debug(
                    "JWT validation failed: {}",
                    e.getClass().getSimpleName()
            );

            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicAuthPath(String path) {
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/notifications/testmail")
                || path.equals("/api/payments/webhook")
                || path.equals("/api/payments/success")
                || path.equals("/api/payments/cancel");
    }

    private boolean isAdminPath(String path) {
        return path.equals("/api/admin")
                || path.startsWith("/api/admin/");
    }

    private Mono<Void> reject(
            ServerWebExchange exchange,
            HttpStatus status
    ) {
        String message = status == HttpStatus.UNAUTHORIZED
                ? "Missing or invalid access token"
                : "You do not have permission to access this resource";

        return errorWriter.write(exchange, status, message);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
