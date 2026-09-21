package com.pulsepass.repository;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // FR-EVT-002: Buscar evento por código único
    Optional<Event> findByEventCode(String eventCode);

    // FR-EVT-005: Eventos publicados ordenados por fecha ascendente
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    // FR-VEN-004: Eventos asociados a un venue por código de venue
    List<Event> findByVenueCode(String venueCode);

    // FR-SRC-001: Buscar eventos donde participe un artista por stageName (JPQL con JOIN)
    @Query("SELECT DISTINCT e FROM Event e JOIN e.artists a WHERE a.stageName = :stageName")
    List<Event> findByArtistStageName(@Param("stageName") String stageName);

    // FR-SRC-002: Buscar eventos de una ciudad en los que participe un artista específico
    @Query("SELECT DISTINCT e FROM Event e JOIN e.artists a WHERE e.venue.city = :city AND a.stageName = :stageName")
    List<Event> findByCityAndArtistStageName(@Param("city") String city, @Param("stageName") String stageName);

    // FR-SRC-003: Eventos recomendados (posteriores a fecha, en ciudad y cuyo artista contenga texto sin importar mayúsculas)
    @Query("SELECT DISTINCT e FROM Event e JOIN e.artists a " +
           "WHERE e.eventDate > :afterDate " +
           "AND e.venue.city = :city " +
           "AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistName, '%')) " +
           "ORDER BY e.eventDate ASC")
    List<Event> findRecommendedEvents(@Param("afterDate") LocalDateTime afterDate, 
                                       @Param("city") String city, 
                                       @Param("artistName") String artistName);
}
