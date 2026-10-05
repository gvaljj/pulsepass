package com.pulsepass.dto.response;

public record ArtistResponse(
        Long id,
        String stageName,
        String genre,
        boolean active
) {}