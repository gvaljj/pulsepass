package com.pulsepass.repository;

import com.pulsepass.domain.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    // PRD §32: buscar venue por código de negocio
    Optional<Venue> findByCode(String code);

    // BR-VENUE-002 / PRD §32: solo venues activos, ordenados por nombre
    List<Venue> findByActiveTrueOrderByNameAsc();
}