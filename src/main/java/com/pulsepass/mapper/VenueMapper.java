package com.pulsepass.mapper;

import com.pulsepass.domain.model.Venue;
import com.pulsepass.dto.response.VenueResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    VenueResponse toResponse(Venue venue);
}
