package com.pulsepass.dto;

import com.pulsepass.domain.enums.EventCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record EventRequest(
    @NotBlank(message = "El código del evento es obligatorio")
    String eventCode,

    @NotBlank(message = "El nombre es obligatorio")
    String name,

    String description,

    @NotNull(message = "La categoría es obligatoria")
    EventCategory category,

    @NotNull(message = "La fecha del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser en el futuro")
    LocalDateTime eventDate,

    @NotNull(message = "La edad mínima es obligatoria")
    @Min(value = 0, message = "La edad mínima debe ser mayor o igual a 0")
    Integer minimumAge,

    @NotBlank(message = "El código del venue es obligatorio")
    String venueCode
) {}