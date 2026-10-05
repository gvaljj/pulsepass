package com.pulsepass.dto.request;

import com.pulsepass.domain.enums.TicketType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PurchaseTicketRequest(
        @NotBlank(message = "El email del usuario es obligatorio")
        @Email(message = "El formato del email no es válido")
        String userEmail,

        @NotBlank(message = "El código del evento es obligatorio")
        String eventCode,

        @NotNull(message = "El tipo de ticket es obligatorio")
        TicketType type
) {}