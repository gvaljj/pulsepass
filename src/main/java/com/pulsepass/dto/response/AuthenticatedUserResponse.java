package com.pulsepass.dto.response;

import java.util.List;

// PRD §9.2 (FR-AUTH-004): identidad del token actual (GET /api/auth/me).
// Los roles se toman de la base de datos vigente, no del JWT.
public record AuthenticatedUserResponse(
        Long id,
        String username,
        String email,
        List<String> roles,
        boolean active
) {}
