package com.pulsepass.service.impl;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Artist;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Venue;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository,
                            VenueRepository venueRepository,
                            ArtistRepository artistRepository,
                            EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.artistRepository = artistRepository;
        this.eventMapper = eventMapper;
    }

    // BR-EVENT-001..006: creación de evento con estado inicial DRAFT (BR-EVENT-005)
    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request) {
        // BR-EVENT-001: código único
        if (eventRepository.existsByEventCode(request.eventCode())) {
            throw new DuplicateResourceException("Ya existe un evento con el código: " + request.eventCode());
        }
        // BR-EVENT-002: venue obligatorio y existente
        Venue venue = venueRepository.findByCode(request.venueCode())
                .orElseThrow(() -> new ResourceNotFoundException("Venue no encontrado: " + request.venueCode()));
        // BR-EVENT-003: el venue debe estar activo
        if (!Boolean.TRUE.equals(venue.getActive())) {
            throw new BusinessRuleException("No se puede crear un evento en un venue inactivo: " + request.venueCode());
        }
        // BR-EVENT-004: la fecha debe ser futura
        if (request.eventDate() == null || !request.eventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("La fecha del evento debe ser futura");
        }
        // BR-EVENT-006: edad mínima >= 0 (null se trata como 0, sin restricción)
        if (request.minimumAge() != null && request.minimumAge() < 0) {
            throw new BusinessRuleException("La edad mínima no puede ser negativa");
        }

        Event event = eventMapper.toEntity(request); // status = DRAFT (BR-EVENT-005)
        event.setVenue(venue);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse findByCode(String eventCode) {
        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventCode));
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> findPublishedEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    // BR-EVENT-007..009: DRAFT -> PUBLISHED validando estado, fecha y venue
    @Override
    @Transactional
    public EventResponse publish(String eventCode) {
        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventCode));

        // BR-EVENT-007: solo se publica un evento en estado DRAFT
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException("Solo se pueden publicar eventos en estado DRAFT");
        }
        // BR-EVENT-008: debe seguir teniendo fecha futura
        if (event.getEventDate() == null || !event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("No se puede publicar un evento con fecha pasada");
        }
        // BR-EVENT-009: el venue debe continuar activo
        if (event.getVenue() == null || !Boolean.TRUE.equals(event.getVenue().getActive())) {
            throw new BusinessRuleException("No se puede publicar un evento cuyo venue está inactivo");
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    // PRD §16: asociar artista a evento, evitando duplicados y estados finales
    @Override
    @Transactional
    public EventResponse addArtist(String eventCode, Long artistId) {
        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventCode));

        // BR-EVENT-011: no se agregan artistas a eventos CANCELLED o FINISHED
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
            throw new BusinessRuleException("No se pueden asociar artistas a un evento " + event.getStatus());
        }

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artista no encontrado con id: " + artistId));

        // BR-EVENT-010: evitar duplicados
        if (event.getArtists().contains(artist)) {
            throw new BusinessRuleException("El artista ya está asociado a este evento: " + artist.getStageName());
        }

        event.getArtists().add(artist);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> findByArtist(String stageName) {
        return eventRepository.findByArtistStageName(stageName)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }
}
