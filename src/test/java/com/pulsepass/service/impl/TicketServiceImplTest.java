package com.pulsepass.service.impl;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserProfile;
import com.pulsepass.domain.model.Venue;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User user;
    private Venue venue;
    private Event event;
    private PurchaseTicketRequest request;

    @BeforeEach
    void setUp() {
        user = new User("andrea", "andrea@email.com");
        user.setActive(true);
        UserProfile profile = new UserProfile();
        profile.setBirthDate(LocalDate.now().minusYears(25));
        user.setProfile(profile);

        venue = new Venue("VEN-01", "Marina", "SM", "Cra 1", 3);

        event = new Event();
        event.setId(1L);
        event.setEventCode("CMF-2026");
        event.setName("Caribbean Music Fest");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(30));
        event.setMinimumAge(18);
        event.setVenue(venue);

        request = new PurchaseTicketRequest("andrea@email.com", "CMF-2026", TicketType.GENERAL);
    }

    private void stubHappyPath(long paidCount) {
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(paidCount);
    }

    private TicketResponse paidResponse() {
        return new TicketResponse(1L, "TCK-1", TicketType.GENERAL, new BigDecimal("50.00"),
                TicketStatus.PAID, LocalDateTime.now(), "andrea@email.com", "CMF-2026", "Caribbean Music Fest");
    }

    // TEST-TICKET-001: compra válida genera ticket PAID
    @Test
    @DisplayName("TEST-TICKET-001: compra válida genera ticket PAID")
    void purchase_Valid_ProducesPaidTicket() {
        stubHappyPath(0);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(paidResponse());

        TicketResponse result = ticketService.purchase(request);

        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertThat(captor.getValue().getPrice()).isGreaterThanOrEqualTo(BigDecimal.ZERO); // BR-TICKET-009
        verify(eventRepository, never()).save(any(Event.class)); // sin SOLD_OUT
    }

    // TEST-TICKET-002: usuario inexistente
    @Test
    @DisplayName("TEST-TICKET-002: usuario inexistente lanza ResourceNotFoundException")
    void purchase_MissingUser_Throws() {
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // TEST-TICKET-003: usuario inactivo
    @Test
    @DisplayName("TEST-TICKET-003: usuario inactivo lanza BusinessRuleException")
    void purchase_InactiveUser_Throws() {
        user.setActive(false);
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // TEST-TICKET-004: evento DRAFT
    @Test
    @DisplayName("TEST-TICKET-004: evento DRAFT lanza BusinessRuleException")
    void purchase_DraftEvent_Throws() {
        event.setStatus(EventStatus.DRAFT);
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // TEST-TICKET-005: evento CANCELLED
    @Test
    @DisplayName("TEST-TICKET-005: evento CANCELLED lanza BusinessRuleException")
    void purchase_CancelledEvent_Throws() {
        event.setStatus(EventStatus.CANCELLED);
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // TEST-TICKET-006: usuario menor de edad
    @Test
    @DisplayName("TEST-TICKET-006: usuario menor de edad lanza BusinessRuleException")
    void purchase_Underage_Throws() {
        user.getProfile().setBirthDate(event.getEventDate().toLocalDate().minusYears(10));
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // TEST-TICKET-007: evento sin capacidad
    @Test
    @DisplayName("TEST-TICKET-007: evento sin capacidad lanza BusinessRuleException")
    void purchase_NoCapacity_Throws() {
        stubHappyPath(3); // capacidad = 3
        assertThatThrownBy(() -> ticketService.purchase(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // TEST-TICKET-008: último ticket disponible guarda y cambia el evento a SOLD_OUT
    @Test
    @DisplayName("TEST-TICKET-008: último ticket guarda y el evento pasa a SOLD_OUT")
    void purchase_LastTicket_SetsSoldOut() {
        stubHappyPath(2); // 2 de 3 -> esta compra completa la capacidad
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(paidResponse());

        ticketService.purchase(request);

        verify(ticketRepository).save(any(Ticket.class));
        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT); // BR-TICKET-008
        verify(eventRepository).save(any(Event.class));
    }

    // TEST-TICKET-009: cancelar ticket PAID -> CANCELLED
    @Test
    @DisplayName("TEST-TICKET-009: cancelar ticket PAID pasa a CANCELLED")
    void cancel_PaidTicket_BecomesCancelled() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);
        ticket.setEvent(event); // fecha futura
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        TicketResponse cancelled = new TicketResponse(1L, "TCK-1", TicketType.GENERAL,
                new BigDecimal("50.00"), TicketStatus.CANCELLED, LocalDateTime.now(),
                "andrea@email.com", "CMF-2026", "Caribbean Music Fest");
        when(ticketMapper.toResponse(ticket)).thenReturn(cancelled);

        TicketResponse result = ticketService.cancel("TCK-1");

        assertThat(result.status()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository).save(any(Ticket.class));
    }

    // TEST-TICKET-010: cancelar ticket USED lanza BusinessRuleException
    @Test
    @DisplayName("TEST-TICKET-010: cancelar ticket USED lanza BusinessRuleException")
    void cancel_UsedTicket_Throws() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.USED);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancel("TCK-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // BR-TICKET-012: no cancelar después de la fecha del evento
    @Test
    @DisplayName("BR-TICKET-012: no cancelar un ticket después de la fecha del evento")
    void cancel_AfterEventDate_Throws() {
        event.setEventDate(LocalDateTime.now().minusDays(1));
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);
        ticket.setEvent(event);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancel("TCK-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // TEST-TICKET-011: marcar PAID como usado -> USED
    @Test
    @DisplayName("TEST-TICKET-011: marcar ticket PAID como usado pasa a USED")
    void markAsUsed_PaidTicket_BecomesUsed() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        TicketResponse used = new TicketResponse(1L, "TCK-1", TicketType.GENERAL,
                new BigDecimal("50.00"), TicketStatus.USED, LocalDateTime.now(),
                "andrea@email.com", "CMF-2026", "Caribbean Music Fest");
        when(ticketMapper.toResponse(ticket)).thenReturn(used);

        TicketResponse result = ticketService.markAsUsed("TCK-1");

        assertThat(result.status()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository).save(any(Ticket.class));
    }

    // TEST-TICKET-012: usar ticket CANCELLED lanza BusinessRuleException
    @Test
    @DisplayName("TEST-TICKET-012: marcar ticket CANCELLED como usado lanza BusinessRuleException")
    void markAsUsed_CancelledTicket_Throws() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.CANCELLED);
        when(ticketRepository.findByTicketCode("TCK-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.markAsUsed("TCK-1"))
                .isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
