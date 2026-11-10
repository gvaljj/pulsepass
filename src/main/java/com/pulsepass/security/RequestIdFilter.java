package com.pulsepass.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// PRD NFR-SEC-006: cada solicitud lleva un request ID correlacionable entre la respuesta y los logs
// (login correcto/fallido y denegaciones 401/403 se registran con ese identificador).
// El id se genera SIEMPRE en el servidor: una cabecera entrante del cliente no se propaga a los logs
// (evita inyección/contaminación de registros). Se registra en la cadena de seguridad ANTES de
// ExceptionTranslationFilter, de modo que su MDC esté activo también durante el registro de las
// denegaciones 401/403 (que ocurren al propagarse la excepción hacia arriba).
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        MDC.put(MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
