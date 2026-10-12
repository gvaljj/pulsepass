package com.pulsepass.dto.response;

import java.time.LocalDate;

// Vista del usuario consolidando User y UserProfile sin exponer entidades JPA
public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String city,
        LocalDate birthDate,
        boolean active
) {}
