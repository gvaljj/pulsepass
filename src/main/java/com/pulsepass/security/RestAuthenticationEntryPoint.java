package com.pulsepass.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// PRD §9.3: todo lo no autenticado (sin Bearer, token expirado o firma incorrecta) -> 401 con cuerpo
// JSON genérico idéntico para todos los casos: no revela existencia de usuario ni la causa exacta.
// NFR-SEC-006: cada denegación se registra en logs con su request ID, sin credenciales ni detalles internos.
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    private final SecurityErrorResponseWriter responseWriter;

    public RestAuthenticationEntryPoint(SecurityErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // NFR-SEC-006: trazabilidad de la denegación; solo método, ruta, clase de la causa y request id
        log.warn("401 No autenticado: {} {} (causa: {}) requestId={}",
                request.getMethod(), request.getRequestURI(),
                authException.getClass().getSimpleName(),
                MDC.get(RequestIdFilter.MDC_KEY));

        response.setHeader("WWW-Authenticate", "Bearer");
        responseWriter.write(response, HttpStatus.UNAUTHORIZED, SecurityErrorMessages.UNAUTHENTICATED);
    }
}
