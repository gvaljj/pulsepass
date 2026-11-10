package com.pulsepass.dto.response;

// PRD §9.2 (FR-AUTH-003): respuesta del login. expiresIn son los segundos de vida del token (TTL configurable).
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
