package com.pulsepass.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// PRD §9.4: autenticado sin permisos sobre el recurso -> 403 con el mensaje estándar del PRD,
// idéntico al que devuelve el GlobalExceptionHandler cuando la denegación nace en un Service.
// NFR-SEC-006: cada denegación se registra en logs con su request ID, sin credenciales ni detalles internos.
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

    private final SecurityErrorResponseWriter responseWriter;

    public RestAccessDeniedHandler(SecurityErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        // NFR-SEC-006: trazabilidad de la denegación; solo método, ruta, principal vigente y request id
        log.warn("403 Acceso denegado: {} {} (principal: {}) requestId={}",
                request.getMethod(), request.getRequestURI(),
                request.getRemoteUser() == null ? "anonimo" : request.getRemoteUser(),
                MDC.get(RequestIdFilter.MDC_KEY));

        responseWriter.write(response, HttpStatus.FORBIDDEN, SecurityErrorMessages.ACCESS_DENIED);
    }
}
