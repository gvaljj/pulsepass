package com.pulsepass.mapper;

import com.pulsepass.domain.model.Event;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class EventMapper {

    @Autowired
    protected ArtistMapper artistMapper;

    // Crea la entidad inicializando el estado en DRAFT (BR-EVENT-005) y sin venue ni artistas todavía
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "artists", ignore = true)
    public abstract Event toEntity(CreateEventRequest request);

    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    public abstract EventResponse toResponse(Event event);

    @Mapping(target = "venueCode", source = "venue.code")
    public abstract EventSummaryResponse toSummary(Event event);
}
