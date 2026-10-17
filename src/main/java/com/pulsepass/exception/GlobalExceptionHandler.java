package com.pulsepass.exception;

import com.pulsepass.dto.response.ErrorResponse;
import com.pulsepass.security.SecurityErrorMessages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// PRD §15: único componente centralizado de manejo de excepciones. Los Controllers no repiten bloques try/catch.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // §14: Bean Validation fallido -> 400 con el detalle de campos inválidos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("Valor inválido"),
                        (first, second) -> first,
                        LinkedHashMap::new));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    // §14: JSON mal formado -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformed(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request", Map.of());
    }

    // §14: recurso inexistente -> 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    // §14: duplicado -> 409
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    // §14: regla de negocio incumplida -> 409
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    // §14 / PRD §9.3: credenciales o token inválidos -> 401 con mensaje GENÉRICO.
    // Nunca distingue usuario inexistente, contraseña incorrecta ni cuenta inactiva (AUTH-004).
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        String message = ex instanceof BadCredentialsException
                ? SecurityErrorMessages.INVALID_CREDENTIALS
                : SecurityErrorMessages.UNAUTHENTICATED;
        return build(HttpStatus.UNAUTHORIZED, message, Map.of());
    }

    // PRD §9.4: autenticado sin permisos sobre el recurso -> 403 con el mensaje estándar del PRD
    // (mismo cuerpo que escribe RestAccessDeniedHandler cuando la denegación nace en el filtro).
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, SecurityErrorMessages.ACCESS_DENIED, Map.of());
    }

    // §14: error inesperado -> 500 sin exponer stack traces ni detalles internos (NFR-CTRL-007)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno no controlado", Map.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Map<String, String> details) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                details);
        return ResponseEntity.status(status).body(body);
    }
}
