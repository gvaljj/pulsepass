package com.pulsepass.repository;

import com.pulsepass.domain.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    
    // FR-ART-002: Buscar artista por nombre artístico
    Optional<Artist> findByStageName(String stageName);
}