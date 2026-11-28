package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

// PRD §9.2/§16: extrae el Bearer del header Authorization, valida el JWT y establece la autenticación.
// Reglas de seguridad del filtro (§9.2):
//  - solo se lee el header Authorization (nunca query string, cookies ni body);
//  - un Bearer inválido/expirado/firmado mal NUNCA se convierte en autenticación (queda para el
//    AuthenticationEntryPoint -> 401 en rutas protegidas, una sola respuesta);
//  - nunca sobrescribe una autenticación ya establecida;
//  - doble cabecera Authorization => solicitud ambigua, no se autentica.
// No se registra como bean (evita el doble registro como Filter del contenedor); se instancia en SecurityConfig.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token != null && isUnauthenticated()) {
            authenticateToken(token, request);
        }
        filterChain.doFilter(request, response);
    }

    // El AnonymousAuthenticationFilter corre ANTES de este filtro (va justo antes de AuthorizationFilter)
    // y deja un AnonymousAuthenticationToken en el contexto: cuenta como "sin autenticar" para que el
    // Bearer sí pueda establecer la identidad real.
    private boolean isUnauthenticated() {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        return current == null || current instanceof AnonymousAuthenticationToken;
    }

    private String extractBearerToken(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(AUTHORIZATION_HEADER);
        if (headers == null || !headers.hasMoreElements()) {
            return null; // sin Bearer: la petición puede continuar si la ruta es pública
        }
        String header = headers.nextElement();
        if (headers.hasMoreElements()) {
            // Doble cabecera Authorization: ambigüedad deliberada -> no se autentica (T-SEC-019)
            log.debug("Solicitud con varias cabeceras Authorization; autenticación omitida");
            return null;
        }
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null; // esquema distinto a Bearer (p. ej. Basic): no se procesa
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private void authenticateToken(String token, HttpServletRequest request) {
        try {
            Claims claims = jwtService.validate(token);
            Long userId = Long.valueOf(claims.getSubject());

            // El principal se reconstruye SIEMPRE desde la base de datos: roles y estado vigentes (SEC-011),
            // el JWT solo aporta el id persistente (sub).
            SecurityPrincipal principal = userDetailsService.loadPrincipalById(userId);
            if (!principal.isEnabled()) {
                log.debug("Token válido pero cuenta desactivada; acceso denegado");
                SecurityContextHolder.clearContext();
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
            // Token presente pero inválido: se limpia el contexto y la cadena continúa.
            // Si la ruta es protegida, el AuthenticationEntryPoint responde 401 (una sola respuesta).
            log.debug("Token JWT descartado: {}", ex.getClass().getSimpleName());
            SecurityContextHolder.clearContext();
        }
    }
}
