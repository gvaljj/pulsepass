package com.pulsepass.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

// PRD §13: contrato JSON uniforme para todos los errores HTTP de la aplicación.
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details
) {}
