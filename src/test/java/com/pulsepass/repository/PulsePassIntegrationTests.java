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

        long paidCount = ticketRepository.countPaidTicketsByEventCode("EVT-COUNT", TicketStatus.PAID);
        assertThat(paidCount).isEqualTo(2);
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