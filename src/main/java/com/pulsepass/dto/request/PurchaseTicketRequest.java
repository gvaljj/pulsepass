package com.pulsepass.dto.request;

import com.pulsepass.domain.enums.TicketType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// PRD §22: campos mínimos de compra. El precio lo define el sistema, nunca el cliente. §11.3: validación estructural en la capa Controller.
public record PurchaseTicketRequest(
        @NotBlank(message = "El email del usuario es obligatorio")
        @Email(message = "El formato del email no es válido")
        String userEmail,

        @NotBlank(message = "El código del evento es obligatorio")
        String eventCode,

        @NotNull(message = "El tipo de ticket es obligatorio")
        TicketType type
) {}
