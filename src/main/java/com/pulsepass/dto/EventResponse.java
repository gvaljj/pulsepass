package com.pulsepass.dto;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import java.time.LocalDateTime;

public record EventResponse(
    Long id,
    String eventCode,
    String name,
    String description,
    EventCategory category,
    EventStatus status,
    LocalDateTime eventDate,
    Integer minimumAge,
    String streamingUrl
) {}