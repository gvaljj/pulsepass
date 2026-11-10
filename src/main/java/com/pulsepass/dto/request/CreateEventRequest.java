package com.pulsepass.dto.request;

import com.pulsepass.domain.enums.EventCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// PRD §12: record con los campos de creación de evento. §11.1: validación estructural en la capa Controller.
public record CreateEventRequest(
        @NotBlank(message = "El código del evento es obligatorio")
        String eventCode,

        @NotBlank(message = "El nombre del evento es obligatorio")
        String name,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        String description,

        @NotNull(message = "La categoría es obligatoria")
        EventCategory category,

        @NotNull(message = "La fecha del evento es obligatoria")
        LocalDateTime eventDate,

        @NotNull(message = "La edad mínima es obligatoria")
        @Min(value = 0, message = "La edad mínima debe ser mayor o igual a 0")
        Integer minimumAge,

        @NotBlank(message = "El código del venue es obligatorio")
        String venueCode
) {}
