package com.pulsepass.dto.request;

import jakarta.validation.constraints.NotBlank;

// PRD §9.2 (FR-AUTH-002): el login acepta username O email en un único campo "identifier".
// No se distingue el tipo de identificador: eso impediría adivinar si existe el usuario.
public record LoginRequest(
        @NotBlank(message = "El username o email es obligatorio")
        String identifier,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
