package com.pulsepass.dto.response;

// Vista del venue sin exponer la entidad JPA
public record VenueResponse(
        Long id,
        String code,
        String name,
        String city,
        String address,
        Integer capacity,
        boolean active
) {}
