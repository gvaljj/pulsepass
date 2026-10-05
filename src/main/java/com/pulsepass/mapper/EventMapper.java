package com.pulsepass.mapper;

import com.pulsepass.dto.EventRequest;
import com.pulsepass.dto.EventResponse;
import com.pulsepass.domain.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "artists", ignore = true)
    Event toEntity(EventRequest request);

    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(target = "artists", ignore = true)
    EventResponse toResponse(Event event);
}