package com.pulsepass.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        LocalDate birthDate
) {}
