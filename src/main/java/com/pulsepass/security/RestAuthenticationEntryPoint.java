package com.pulsepass.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// PRD §9.3: todo lo no autenticado (sin Bearer, token expirado o firma incorrecta) -> 401 con cuerpo
// JSON genérico idéntico para todos los casos: no revela existencia de usuario ni la causa exacta.
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter responseWriter;

    public RestAuthenticationEntryPoint(SecurityErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        responseWriter.write(response, HttpStatus.UNAUTHORIZED, SecurityErrorMessages.UNAUTHENTICATED);
    }
}
