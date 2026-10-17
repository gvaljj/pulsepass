package com.pulsepass.service.impl;

import com.pulsepass.domain.enums.EventCategory;
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
import com.pulsepass.service.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private VenueRepository venueRepository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private EventMapper eventMapper;
    // Fase 4: visibilidad de borradores delegada en la frontera de autorización (mock = no-op por defecto)
    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private EventServiceImpl eventService;

    private Venue venue;
    private Event event;
    private CreateEventRequest createRequest;
    private EventResponse eventResponse;

    @BeforeEach
    void setUp() {
        venue = new Venue("VEN-01", "Marina Convention Center", "Santa Marta", "Cra 1", 3);
        venue.setActive(true);

        event = new Event();
        event.setId(1L);
        event.setEventCode("CMF-2026");
        event.setName("Caribbean Music Fest 2026");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.DRAFT);
        event.setEventDate(LocalDateTime.now().plusDays(30));
        event.setMinimumAge(18);
        event.setVenue(venue);

        createRequest = new CreateEventRequest(
                "CMF-2026",
                "Caribbean Music Fest 2026",
                "Festival",
                EventCategory.MUSIC,
                LocalDateTime.now().plusDays(30),
                18,
                "VEN-01"
        );

        eventResponse = new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, event.getEventDate(), 18,
                "VEN-01", "Marina Convention Center", List.of()
        );
    }

    // TEST-EVENT-001: evento existente retorna DTO
    @Test
    @DisplayName("TEST-EVENT-001: evento existente retorna DTO")
    void findByCode_ExistingEvent_ReturnsDto() {
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventService.findByCode("CMF-2026");

        assertThat(result.eventCode()).isEqualTo("CMF-2026");
    }

    // TEST-EVENT-002: evento inexistente lanza ResourceNotFoundException
    @Test
    @DisplayName("TEST-EVENT-002: evento inexistente lanza ResourceNotFoundException")
    void findByCode_MissingEvent_Throws() {
        when(eventRepository.findByEventCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findByCode("NOPE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // TEST-EVENT-003: crear evento válido ejecuta save()
    @Test
    @DisplayName("TEST-EVENT-003: crear evento válido guarda y retorna DTO")
    void create_ValidEvent_Saves() {
        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-01")).thenReturn(Optional.of(venue));
        when(eventMapper.toEntity(createRequest)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventService.create(createRequest);

        assertThat(result.eventCode()).isEqualTo("CMF-2026");
        verify(eventRepository).save(any(Event.class));
    }

    // BR-EVENT-001: código duplicado lanza DuplicateResourceException
    @Test
    @DisplayName("BR-EVENT-001: código duplicado lanza DuplicateResourceException")
    void create_DuplicateCode_Throws() {
        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(true);

        assertThatThrownBy(() -> eventService.create(createRequest))
                .isInstanceOf(DuplicateResourceException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // TEST-EVENT-004: venue inexistente lanza error y nunca guarda
    @Test
    @DisplayName("TEST-EVENT-004: venue inexistente lanza ResourceNotFoundException y no guarda")
    void create_MissingVenue_ThrowsAndDoesNotSave() {
        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // TEST-EVENT-005: venue inactivo lanza BusinessRuleException
    @Test
    @DisplayName("TEST-EVENT-005: venue inactivo lanza BusinessRuleException y no guarda")
    void create_InactiveVenue_Throws() {
        venue.setActive(false);
        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-01")).thenReturn(Optional.of(venue));

        assertThatThrownBy(() -> eventService.create(createRequest))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // TEST-EVENT-006: fecha pasada lanza BusinessRuleException
    @Test
    @DisplayName("TEST-EVENT-006: fecha pasada lanza BusinessRuleException y no guarda")
    void create_PastDate_Throws() {
        CreateEventRequest past = new CreateEventRequest(
                "CMF-2026", "Festival", "Festival", EventCategory.MUSIC,
                LocalDateTime.now().minusDays(1), 18, "VEN-01");
        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-01")).thenReturn(Optional.of(venue));

        assertThatThrownBy(() -> eventService.create(past))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // TEST-EVENT-007: publicar DRAFT válido pasa a PUBLISHED
    @Test
    @DisplayName("TEST-EVENT-007: publicar DRAFT válido pasa a PUBLISHED")
    void publish_DraftEvent_BecomesPublished() {
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        EventResponse published = new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, event.getEventDate(), 18,
                "VEN-01", "Marina Convention Center", List.of());
        when(eventMapper.toResponse(event)).thenReturn(published);

        EventResponse result = eventService.publish("CMF-2026");

        assertThat(result.status()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(any(Event.class));
    }

    // TEST-EVENT-008: publicar CANCELLED lanza BusinessRuleException y no persiste
    @Test
    @DisplayName("TEST-EVENT-008: publicar evento CANCELLED lanza BusinessRuleException")
    void publish_CancelledEvent_Throws() {
        event.setStatus(EventStatus.CANCELLED);
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.publish("CMF-2026"))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // BR-EVENT-010: no asociar el mismo artista dos veces
    @Test
    @DisplayName("BR-EVENT-010: asociar artista duplicado lanza BusinessRuleException")
    void addArtist_DuplicateArtist_Throws() {
        Artist artist = new Artist("Solar Beat", "CO", "Pop");
        event.setArtists(new java.util.HashSet<>(Set.of(artist)));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> eventService.addArtist("CMF-2026", 1L))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // BR-EVENT-011: no asociar artistas a eventos CANCELLED/FINISHED
    @Test
    @DisplayName("BR-EVENT-011: no asociar artistas a un evento CANCELLED")
    void addArtist_CancelledEvent_Throws() {
        event.setStatus(EventStatus.CANCELLED);
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.addArtist("CMF-2026", 1L))
                .isInstanceOf(BusinessRuleException.class);
        verify(artistRepository, never()).findById(any());
        verify(eventRepository, never()).save(any(Event.class));
    }

    // addArtist feliz: asocia y retorna DTO
    @Test
    @DisplayName("addArtist: asocia un artista nuevo al evento")
    void addArtist_NewArtist_Associates() {
        Artist artist = new Artist("Neon Waves", "CO", "Electro");
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(artistRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        eventService.addArtist("CMF-2026", 2L);

        assertThat(event.getArtists()).contains(artist);
        verify(eventRepository).save(any(Event.class));
    }

    // findByArtist: retorna resúmenes
    @Test
    @DisplayName("findByArtist: retorna los resúmenes de eventos publicados del artista")
    void findByArtist_ReturnsSummaries() {
        event.setStatus(EventStatus.PUBLISHED); // la búsqueda pública solo expone eventos publicados (EVT-06)
        EventSummaryResponse summary = new EventSummaryResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026",
                EventCategory.MUSIC, EventStatus.PUBLISHED, event.getEventDate(), "VEN-01");
        when(eventRepository.findByArtistStageName("Solar Beat")).thenReturn(List.of(event));
        when(eventMapper.toSummary(event)).thenReturn(summary);

        List<EventSummaryResponse> result = eventService.findByArtist("Solar Beat");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).eventCode()).isEqualTo("CMF-2026");
    }

    // EVT-06 / T-SEC-017: los borradores no se exponen a identidades no privilegiadas
    @Test
    @DisplayName("findByArtist: excluye borradores para identidades no privilegiadas y los muestra a STAFF/ADMIN")
    void findByArtist_HidesDraftsFromPublicUsers() {
        // event del setUp está en DRAFT
        when(eventRepository.findByArtistStageName("Solar Beat")).thenReturn(List.of(event));

        // sin privilegio (mock por defecto false): el borrador se filtra
        assertThat(eventService.findByArtist("Solar Beat")).isEmpty();

        // STAFF/ADMIN (isPrivileged = true): sí ve el borrador
        when(authorizationService.isPrivileged()).thenReturn(true);
        EventSummaryResponse summary = new EventSummaryResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026",
                EventCategory.MUSIC, EventStatus.DRAFT, event.getEventDate(), "VEN-01");
        when(eventMapper.toSummary(event)).thenReturn(summary);

        assertThat(eventService.findByArtist("Solar Beat")).hasSize(1);
    }
}
