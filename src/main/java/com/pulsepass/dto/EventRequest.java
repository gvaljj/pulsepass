package com.pulsepass.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record EventRequest(
    @NotBlank(message = "El título es obligatorio")
    String title,

    String description,

    @NotNull(message = "La fecha es obligatoria")
    @Future(message = "La fecha del evento debe ser en el futuro")
    LocalDateTime date,

    @NotBlank(message = "La ubicación es obligatoria")
    String location,

    @NotNull(message = "El total de tickets es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 ticket disponible")
    Integer totalTickets
) {}