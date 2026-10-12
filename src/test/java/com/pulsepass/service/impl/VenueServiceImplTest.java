package com.pulsepass.service.impl;

import com.pulsepass.domain.model.Venue;
import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.VenueMapper;
import com.pulsepass.repository.VenueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock
    private VenueRepository venueRepository;
    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private VenueServiceImpl venueService;

    // BR-VENUE-001: venue existente retorna DTO
    @Test
    @DisplayName("BR-VENUE-001: venue existente retorna DTO")
    void findByCode_ExistingVenue_ReturnsDto() {
        Venue venue = new Venue("VEN-01", "Marina", "SM", "Cra 1", 100);
        VenueResponse response = new VenueResponse(1L, "VEN-01", "Marina", "SM", "Cra 1", 100, true);
        when(venueRepository.findByCode("VEN-01")).thenReturn(Optional.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(response);

        VenueResponse result = venueService.findByCode("VEN-01");

        assertThat(result.code()).isEqualTo("VEN-01");
    }

    // BR-VENUE-001: venue inexistente lanza ResourceNotFoundException
    @Test
    @DisplayName("BR-VENUE-001: venue inexistente lanza ResourceNotFoundException")
    void findByCode_MissingVenue_Throws() {
        when(venueRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.findByCode("NOPE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // BR-VENUE-002: solo venues activos
    @Test
    @DisplayName("BR-VENUE-002: findActiveVenues retorna solo activos")
    void findActiveVenues_ReturnsActive() {
        Venue venue = new Venue("VEN-01", "Marina", "SM", "Cra 1", 100);
        VenueResponse response = new VenueResponse(1L, "VEN-01", "Marina", "SM", "Cra 1", 100, true);
        when(venueRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(response);

        List<VenueResponse> result = venueService.findActiveVenues();

        assertThat(result).hasSize(1);
    }
}
