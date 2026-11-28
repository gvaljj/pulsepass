package com.pulsepass.security;

// PRD §9.3 (401 genérico) y §9.4 (403): mensajes de error estables y no reveladores,
// compartidos por el AuthenticationEntryPoint, el AccessDeniedHandler y el GlobalExceptionHandler
// para que el cuerpo JSON sea idéntico venga de donde venga la denegación.
public final class SecurityErrorMessages {

    // 401: token ausente, malformado, expirado o firmado incorrectamente / login fallido
    public static final String UNAUTHENTICATED = "Se requiere autenticación válida";

    // 401 específico del login: nunca distingue usuario inexistente de contraseña incorrecta (AUTH-004)
    public static final String INVALID_CREDENTIALS = "Credenciales inválidas";

    // 403: autenticado sin permisos sobre el recurso solicitado
    public static final String ACCESS_DENIED = "No tiene permisos para realizar esta operación";

    private SecurityErrorMessages() {}
}
