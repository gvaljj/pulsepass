package com.pulsepass.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// PRD §19: record con los campos de registro de usuario. §11.2: validación estructural en la capa Controller.
public record RegisterUserRequest(
        @NotBlank(message = "El username es obligatorio")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        String email,

        @NotBlank(message = "El nombre es obligatorio")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        String lastName,

        String phone,

        String city,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        LocalDate birthDate,

        // PRD §9.1/§9.3: contraseña obligatoria, 12..72 caracteres (BCrypt admite hasta 72 bytes).
        // Nunca se persiste en claro: se guarda solo el hash BCrypt en user_credentials (V5).
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 12, max = 72, message = "La contraseña debe tener entre 12 y 72 caracteres")
        String password
) {}
