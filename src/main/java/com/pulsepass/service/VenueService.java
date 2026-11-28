package com.pulsepass.service;

import com.pulsepass.dto.response.VenueResponse;
import java.util.List;

// PRD §10.1: contrato mínimo de VenueService
public interface VenueService {
    VenueResponse findByCode(String code);
    List<VenueResponse> findActiveVenues();
}
