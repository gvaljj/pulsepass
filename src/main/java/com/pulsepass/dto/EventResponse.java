package com.pulsepass.dto;

import java.time.LocalDateTime;

public record EventResponse(
    Long id,
    String title,
    String description,
    LocalDateTime date,
    String location,
    Integer totalTickets,
    Integer availableTickets
) {}