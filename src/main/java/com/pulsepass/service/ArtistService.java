package com.pulsepass.service;

import com.pulsepass.dto.response.ArtistResponse;
import java.util.List;

// PRD §17: contrato mínimo de ArtistService
public interface ArtistService {
    ArtistResponse findById(Long id);
    ArtistResponse findByStageName(String stageName);
    List<ArtistResponse> findActiveArtists();
}
