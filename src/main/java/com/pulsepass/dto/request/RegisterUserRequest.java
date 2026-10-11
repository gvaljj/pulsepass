package com.pulsepass.dto.request;

import java.time.LocalDate;

// PRD §19: record con los campos de registro de usuario (User + UserProfile)
public record RegisterUserRequest(
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String city,
        LocalDate birthDate
) {}
