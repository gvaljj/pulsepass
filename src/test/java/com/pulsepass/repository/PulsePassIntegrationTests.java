package com.pulsepass.repository;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@Transactional
class PulsePassIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("pulsepass_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("AC-001: Registrar Venue válido y recuperarlo por código")
    void testRegistrarVenueValido() {
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Calle 10 #2-15", 5000);
        venueRepository.save(venue);

        Optional<Venue> retrieved = venueRepository.findByCode("VEN-SMR-01");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCapacity()).isGreaterThan(0);
    }

    @Test
    @DisplayName("AC-002: Buscar evento por eventCode y recuperar su Venue")
    void testBuscarEventoYVenue() {
        Venue venue = venueRepository.save(new Venue("VEN-002", "Estadio Central", "Bogotá", "Av 68", 30000));

        Event event = new Event();
        event.setEventCode("CMF-2026");
        event.setName("Caribbean Music Fest 2026");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(30));
        event.setVenue(venue);
        eventRepository.save(event);

        Optional<Event> foundEvent = eventRepository.findByEventCode("CMF-2026");
        assertThat(foundEvent).isPresent();
        assertThat(foundEvent.get().getVenue().getCode()).isEqualTo("VEN-002");
    }

    @Test
    @DisplayName("AC-004: Restricción UNIQUE 1:1 en UserProfile por PostgreSQL")
    void testUserProfileUnicidadViolacion() {
        User user = userRepository.save(new User("johndoe", "john@example.com"));

        UserProfile profile1 = new UserProfile();
        profile1.setFirstName("John");
        profile1.setLastName("Doe");
        profile1.setUser(user);
        userProfileRepository.saveAndFlush(profile1);

        // Intentar guardar un segundo perfil con el mismo user_id en la base de datos debe fallar por FK UNIQUE
        UserProfile profile2 = new UserProfile();
        profile2.setFirstName("Johnny");
        profile2.setLastName("Doe");
        profile2.setUser(user);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userProfileRepository.saveAndFlush(profile2);
        });
    }

    @Test
    @DisplayName("AC-005: Restricción UNIQUE en ticketCode por PostgreSQL")
    void testTicketCodeUnicidadViolacion() {
        Venue venue = venueRepository.save(new Venue("VEN-003", "Arena", "Medellín", "Calle 50", 10000));
        User user = userRepository.save(new User("marta_g", "marta@example.com"));

        Event event = new Event();
        event.setEventCode("EVT-UNIQUE");
        event.setName("Concierto Rock");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(10));
        event.setVenue(venue);
        eventRepository.save(event);

        Ticket ticket1 = new Ticket();
        ticket1.setTicketCode("TCK-0001");
        ticket1.setType(TicketType.VIP);
        ticket1.setPrice(new BigDecimal("250000.00"));
        ticket1.setStatus(TicketStatus.PAID);
        ticket1.setPurchaseDate(LocalDateTime.now());
        ticket1.setUser(user);
        ticket1.setEvent(event);
        ticketRepository.saveAndFlush(ticket1);

        Ticket ticket2 = new Ticket();
        ticket2.setTicketCode("TCK-0001"); // Mismo código duplicado
        ticket2.setType(TicketType.GENERAL);
        ticket2.setPrice(new BigDecimal("120000.00"));
        ticket2.setStatus(TicketStatus.PAID);
        ticket2.setPurchaseDate(LocalDateTime.now());
        ticket2.setUser(user);
        ticket2.setEvent(event);

        assertThrows(DataIntegrityViolationException.class, () -> {
            ticketRepository.saveAndFlush(ticket2);
        });
    }

    @Test
    @DisplayName("AC-008: Conteo exclusivo de tickets PAID por evento")
    void testConteoTicketsPaid() {
        Venue venue = venueRepository.save(new Venue("VEN-004", "Teatro", "Cali", "Cra 5", 2000));
        User user = userRepository.save(new User("carlos_p", "carlos@example.com"));

        Event event = new Event();
        event.setEventCode("EVT-COUNT");
        event.setName("Festival Jazz");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(5));
        event.setVenue(venue);
        eventRepository.save(event);

        // Crear 2 tickets PAID y 1 RESERVED
        crearTicket("TCK-P1", TicketStatus.PAID, user, event);
        crearTicket("TCK-P2", TicketStatus.PAID, user, event);
        crearTicket("TCK-R1", TicketStatus.RESERVED, user, event);

        long paidCount = ticketRepository.countByEventEventCodeAndStatus("EVT-COUNT", TicketStatus.PAID);
        assertThat(paidCount).isEqualTo(2);
    }

    @Test
    @DisplayName("AC-003: Evento con tres artistas sin duplicar el par evento-artista (QT-005)")
    void testEventArtistTresArtistasSinDuplicados() {
        Venue venue = venueRepository.save(new Venue("VEN-005", "Plaza Mayor", "Barranquilla", "Cra 53 #80-100", 8000));

        // Artistas sembrados por V2__insert_initial_artists.sql (valida QT-001 y la relación N:M)
        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageName("Neon Waves").orElseThrow();
        Artist caribbeanSound = artistRepository.findByStageName("Caribbean Sound").orElseThrow();

        Event event = new Event();
        event.setEventCode("AC3-EVT");
        event.setName("Festival Triple Estelar");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(20));
        event.setVenue(venue);
        event.getArtists().add(solarBeat);
        event.getArtists().add(neonWaves);
        event.getArtists().add(caribbeanSound);
        eventRepository.saveAndFlush(event);

        Event reloaded = eventRepository.findByEventCode("AC3-EVT").orElseThrow();
        assertThat(reloaded.getArtists()).hasSize(3);

        // Reintentar la misma asociación no duplica el par evento-artista (Set + PK compuesta)
        reloaded.getArtists().add(solarBeat);
        eventRepository.saveAndFlush(reloaded);
        assertThat(eventRepository.findByEventCode("AC3-EVT").orElseThrow().getArtists()).hasSize(3);
    }

    @Test
    @DisplayName("AC-006: La consulta de eventos publicados excluye DRAFT y CANCELLED")
    void testEventosPublicadosExcluyenOtrosEstados() {
        Venue venue = venueRepository.save(new Venue("VEN-006", "Coliseo Norte", "Cartagena", "Cl 30 #17-40", 12000));

        crearEvento("AC6-DRAFT", "Borrador", EventStatus.DRAFT, LocalDateTime.now().plusDays(40), venue);
        crearEvento("AC6-PUB", "Publicado", EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), venue);
        crearEvento("AC6-CXL", "Cancelado", EventStatus.CANCELLED, LocalDateTime.now().plusDays(20), venue);

        List<Event> publishedEvents = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);

        assertThat(publishedEvents).extracting(Event::getStatus).containsOnly(EventStatus.PUBLISHED);
        assertThat(publishedEvents).extracting(Event::getEventCode)
                .contains("AC6-PUB")
                .doesNotContain("AC6-DRAFT", "AC6-CXL");
    }

    @Test
    @DisplayName("AC-007: Los eventos de un artista aparecen una sola vez (sin duplicados)")
    void testBuscarEventosPorArtistaSinDuplicados() {
        Venue venue = venueRepository.save(new Venue("VEN-007", "Anfiteatro Caribe", "Santa Marta", "Blvd del Mar #5", 4000));

        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageName("Neon Waves").orElseThrow();

        // Dos eventos con Solar Beat; el primero además con otro artista
        Event eventA = crearEvento("AC7-EVT-A", "Noche Electrónica", EventStatus.PUBLISHED,
                LocalDateTime.now().plusDays(15), venue);
        eventA.getArtists().add(solarBeat);
        eventA.getArtists().add(neonWaves);

        Event eventB = crearEvento("AC7-EVT-B", "Solar Beat Live", EventStatus.PUBLISHED,
                LocalDateTime.now().plusDays(25), venue);
        eventB.getArtists().add(solarBeat);

        eventRepository.saveAndFlush(eventA);
        eventRepository.saveAndFlush(eventB);

        List<Event> found = eventRepository.findByArtistStageName("Solar Beat");

        assertThat(found).extracting(Event::getEventCode)
                .containsExactlyInAnyOrder("AC7-EVT-A", "AC7-EVT-B");
        assertThat(found).extracting(Event::getEventCode).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("FR-SRC-003: Recomendados solo PUBLISHED, futuros, por ciudad y artista case-insensitive")
    void testEventosRecomendadosFiltranPublicados() {
        Venue santaMarta = venueRepository.save(new Venue("VEN-S003", "Convention Pier", "Santa Marta", "Calle 1 #2-3", 5000));
        Venue bogota = venueRepository.save(new Venue("VEN-B003", "Arena Sabana", "Bogotá", "Av 68 #24-15", 25000));

        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();

        // Único que cumple todos los criterios
        Event recommended = crearEvento("SRC3-OK", "Recomendado", EventStatus.PUBLISHED,
                LocalDateTime.now().plusDays(30), santaMarta);
        recommended.getArtists().add(solarBeat);

        // Excluido: DRAFT (FR-SRC-003 exige publicados)
        Event draft = crearEvento("SRC3-DRAFT", "Borrador", EventStatus.DRAFT,
                LocalDateTime.now().plusDays(30), santaMarta);
        draft.getArtists().add(solarBeat);

        // Excluido: CANCELLED
        Event cancelled = crearEvento("SRC3-CXL", "Cancelado", EventStatus.CANCELLED,
                LocalDateTime.now().plusDays(30), santaMarta);
        cancelled.getArtists().add(solarBeat);

        // Excluido: fecha pasada
        Event past = crearEvento("SRC3-PAST", "Pasado", EventStatus.PUBLISHED,
                LocalDateTime.now().minusDays(5), santaMarta);
        past.getArtists().add(solarBeat);

        // Excluido: otra ciudad
        Event otherCity = crearEvento("SRC3-CITY", "Otra ciudad", EventStatus.PUBLISHED,
                LocalDateTime.now().plusDays(30), bogota);
        otherCity.getArtists().add(solarBeat);

        eventRepository.saveAndFlush(recommended);

        // Búsqueda con "solar" en minúsculas valida el LOWER(...) case-insensitive
        List<Event> recs = eventRepository.findRecommendedEvents(
                LocalDateTime.now(), "Santa Marta", "solar");

        assertThat(recs).extracting(Event::getEventCode).containsExactly("SRC3-OK");
    }

    @Test
    @DisplayName("FR-TKT-006: Tickets de un usuario por email con y sin filtro de estado")
    void testTicketsPorEmailConYSinEstado() {
        Venue venue = venueRepository.save(new Venue("VEN-008", "Teatro del Mar", "Santa Marta", "Cra 5 #12-30", 1500));
        User user = userRepository.save(new User("andrea_v", "andrea@example.com"));
        Event event = crearEvento("TKT-008", "Concierto Prueba", EventStatus.PUBLISHED,
                LocalDateTime.now().plusDays(12), venue);

        crearTicket("TCK-F1", TicketStatus.PAID, user, event);
        crearTicket("TCK-F2", TicketStatus.PAID, user, event);
        crearTicket("TCK-F3", TicketStatus.RESERVED, user, event);

        List<Ticket> allTickets = ticketRepository.findByUserEmail("andrea@example.com");
        List<Ticket> reservedTickets = ticketRepository.findByUserEmailAndStatus("andrea@example.com", TicketStatus.RESERVED);

        assertThat(allTickets).hasSize(3);
        assertThat(reservedTickets).hasSize(1);
        assertThat(reservedTickets.get(0).getStatus()).isEqualTo(TicketStatus.RESERVED);
    }

    private Event crearEvento(String code, String name, EventStatus status, LocalDateTime date, Venue venue) {
        Event event = new Event();
        event.setEventCode(code);
        event.setName(name);
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(status);
        event.setEventDate(date);
        event.setVenue(venue);
        return eventRepository.save(event);
    }

    private void crearTicket(String code, TicketStatus status, User user, Event event) {
        Ticket ticket = new Ticket();
        ticket.setTicketCode(code);
        ticket.setType(TicketType.GENERAL);
        ticket.setPrice(new BigDecimal("100000.00"));
        ticket.setStatus(status);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);
        ticketRepository.save(ticket);
    }
}