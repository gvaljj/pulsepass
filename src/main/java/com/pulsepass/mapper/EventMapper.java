package com.pulsepass.mapper;

import com.pulsepass.dto.EventRequest;
import com.pulsepass.dto.EventResponse;
import com.pulsepass.domain.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventResponse toResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "artists", ignore = true)
    Event toEntity(EventRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "artists", ignore = true)
    void updateEntityFromRequest(EventRequest request, @MappingTarget Event event);
}