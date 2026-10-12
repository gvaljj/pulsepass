package com.pulsepass.service.impl;

import com.pulsepass.domain.model.Artist;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.ArtistMapper;
import com.pulsepass.repository.ArtistRepository;
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
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private ArtistMapper artistMapper;

    @InjectMocks
    private ArtistServiceImpl artistService;

    // BR-ARTIST-001: artista existente retorna DTO
    @Test
    @DisplayName("BR-ARTIST-001: artista existente retorna DTO")
    void findById_ExistingArtist_ReturnsDto() {
        Artist artist = new Artist("Solar Beat", "CO", "Pop");
        ArtistResponse response = new ArtistResponse(1L, "Solar Beat", "Pop", true);
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response);

        ArtistResponse result = artistService.findById(1L);

        assertThat(result.stageName()).isEqualTo("Solar Beat");
    }

    // BR-ARTIST-001: artista inexistente lanza ResourceNotFoundException
    @Test
    @DisplayName("BR-ARTIST-001: artista inexistente lanza ResourceNotFoundException")
    void findByStageName_MissingArtist_Throws() {
        when(artistRepository.findByStageNameIgnoreCase("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findByStageName("NOPE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // BR-ARTIST-002: solo artistas activos
    @Test
    @DisplayName("BR-ARTIST-002: findActiveArtists retorna solo activos")
    void findActiveArtists_ReturnsActive() {
        Artist artist = new Artist("Solar Beat", "CO", "Pop");
        ArtistResponse response = new ArtistResponse(1L, "Solar Beat", "Pop", true);
        when(artistRepository.findByActiveTrueOrderByStageNameAsc()).thenReturn(List.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response);

        List<ArtistResponse> result = artistService.findActiveArtists();

        assertThat(result).hasSize(1);
    }
}
