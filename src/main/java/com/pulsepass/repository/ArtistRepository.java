package com.pulsepass.repository;

import com.pulsepass.domain.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // Usado por la capa de persistencia (integración)
    Optional<Artist> findByStageName(String stageName);

    // PRD §32: buscar artista por nombre artístico ignorando mayúsculas
    Optional<Artist> findByStageNameIgnoreCase(String stageName);

    // BR-ARTIST-002 / PRD §32: solo artistas activos, ordenados por nombre artístico
    List<Artist> findByActiveTrueOrderByStageNameAsc();
}