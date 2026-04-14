package com.service.apigateway.filter;

import com.service.apigateway.config.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ✅ 1. ALLOW AUTH SERVICE (NO TOKEN)
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // ✅ 2. ALLOW SWAGGER (ALL SERVICES)
        if (path.contains("/v3/api-docs") ||
            path.contains("/swagger-ui") ||
            path.contains("/swagger-ui.html") ||
            path.contains("/webjars")) {

            return chain.filter(exchange);
        }

        // 🔐 3. PROTECT PROVIDER SERVICE
        if (path.startsWith("/providers/")) {

            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing token", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = jwtUtil.extractClaims(token);

                if (!jwtUtil.isValid(token)) {
                    return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                }

                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);

                // 🔥 DEBUG (optional)
                System.out.println("==== JWT DEBUG ====");
                System.out.println("PATH  : " + path);
                System.out.println("USER  : " + userId);
                System.out.println("ROLE  : " + role);
                System.out.println("EMAIL : "+email);

                // ✅ PASS DATA TO DOWNSTREAM SERVICE
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId != null ? userId : "")
                        .header("X-User-Email", email != null ? email : "")
                        .header("X-User-Role", role != null ? role : "")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        }

        // 🔓 4. DEFAULT (ALLOW OTHER REQUESTS)
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String msg, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}