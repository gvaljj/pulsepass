package com.pulsepass.dto.request;

import com.pulsepass.domain.enums.EventCategory;
import java.time.LocalDateTime;

// PRD §12: record con los campos de creación de evento
public record CreateEventRequest(
        String eventCode,
        String name,
        String description,
        EventCategory category,
        LocalDateTime eventDate,
        Integer minimumAge,
        String venueCode
) {}
