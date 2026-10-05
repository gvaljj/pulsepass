package com.pulsepass.dto;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import jakarta.validation.constraints.Future;
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

    @NotNull(message = "El estado es obligatorio")
    EventStatus status,

    @NotNull(message = "La fecha del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser en el futuro")
    LocalDateTime eventDate,

    Integer minimumAge,

    String streamingUrl
) {}