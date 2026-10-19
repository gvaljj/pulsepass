package com.pulsepass.dto.response;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import java.time.LocalDateTime;
import java.util.List;

// PRD §13: expone el evento completo sin entidades JPA (Venue, Set<Artist>, List<Ticket>)
public record EventResponse(
        Long id,
        String eventCode,
        String name,
        String description,
        EventCategory category,
        EventStatus status,
        LocalDateTime eventDate,
        Integer minimumAge,
        String venueCode,
        String venueName,
        List<ArtistResponse> artists
) {}
