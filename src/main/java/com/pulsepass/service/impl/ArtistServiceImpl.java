package com.pulsepass.service.impl;

import com.pulsepass.domain.model.Artist;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.ArtistMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.service.ArtistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;

    public ArtistServiceImpl(ArtistRepository artistRepository, ArtistMapper artistMapper) {
        this.artistRepository = artistRepository;
        this.artistMapper = artistMapper;
    }

    // BR-ARTIST-001: si el artista no existe se lanza ResourceNotFoundException
    @Override
    @Transactional(readOnly = true)
    public ArtistResponse findById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista no encontrado con id: " + id));
        return artistMapper.toResponse(artist);
    }

    // BR-ARTIST-001: buscar por nombre artístico ignorando mayúsculas
    @Override
    @Transactional(readOnly = true)
    public ArtistResponse findByStageName(String stageName) {
        Artist artist = artistRepository.findByStageNameIgnoreCase(stageName)
                .orElseThrow(() -> new ResourceNotFoundException("Artista no encontrado: " + stageName));
        return artistMapper.toResponse(artist);
    }

    // BR-ARTIST-002: solo artistas activos
    @Override
    @Transactional(readOnly = true)
    public List<ArtistResponse> findActiveArtists() {
        return artistRepository.findByActiveTrueOrderByStageNameAsc()
                .stream()
                .map(artistMapper::toResponse)
                .toList();
    }
}
