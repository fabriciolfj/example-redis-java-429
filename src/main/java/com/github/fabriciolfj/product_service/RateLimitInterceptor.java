package com.github.fabriciolfj.product_service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterConfig rateLimit;

    private static final int DEFAULT_TOKENS_PER_REQUEST = 1;
    private static final double DEFAULT_REFILL_RATE = 2.0; // tokens por segundo
    private static final int DEFAULT_CAPACITY = 5;  // Capacidade máxima do bucket

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientIdentifier(request);
        boolean allowed = rateLimit.allowRequest(
                clientId,
                DEFAULT_TOKENS_PER_REQUEST,
                DEFAULT_REFILL_RATE,
                DEFAULT_CAPACITY
        );

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return false;
        }

        return true;
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // Poderia usar o IP, o usuário autenticado, ou um token de API
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) {
            return "api:" + apiKey;
        }

        // Fallback para IP do cliente
        return "ip:" + request.getRemoteAddr();
    }
}