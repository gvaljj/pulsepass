package com.pulsepass.repository;

import com.pulsepass.domain.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    
    // FR-VEN-001: Buscar venue por código de negocio
    Optional<Venue> findByCode(String code);
}