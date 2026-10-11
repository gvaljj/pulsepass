package com.pulsepass.service.impl;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    // Precio base académico. La estrategia de precio vive encapsulada en calculatePrice (BR-TICKET-009).
    private static final BigDecimal PRECIO_BASE = new BigDecimal("50.00");

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketMapper = ticketMapper;
    }

    // Flujo de compra atómico (PRD §24 y §39): usuario -> evento -> edad -> capacidad -> precio -> ticket -> SOLD_OUT
    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {
        // BR-TICKET-001: usuario existente
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + request.userEmail()));
        // BR-TICKET-002: usuario activo
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessRuleException("El usuario está inactivo y no puede comprar tickets");
        }

        // BR-TICKET-003: evento existente
        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + request.eventCode()));
        // BR-TICKET-004: solo eventos PUBLISHED
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException("Solo se pueden comprar tickets para eventos PUBLISHED");
        }
        // BR-TICKET-005: fecha futura
        if (event.getEventDate() == null || !event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("No se puede comprar para un evento que ya ocurrió");
        }
        // BR-TICKET-006: edad mínima evaluada en la fecha del evento usando UserProfile.birthDate
        validateMinimumAge(user, event);

        // BR-TICKET-007: capacidad del venue
        long paidTickets = ticketRepository.countByEventEventCodeAndStatus(event.getEventCode(), TicketStatus.PAID);
        int capacity = event.getVenue().getCapacity();
        if (paidTickets >= capacity) {
            throw new BusinessRuleException("El evento se encuentra agotado (sin capacidad disponible)");
        }

        // Precio calculado por el sistema, nunca confiado desde el cliente (PRD §22)
        Ticket ticket = new Ticket();
        ticket.setTicketCode(generateTicketCode());
        ticket.setType(request.type());
        ticket.setPrice(calculatePrice(request.type()));
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);

        Ticket saved = ticketRepository.save(ticket);

        // BR-TICKET-008: si esta compra completa la capacidad, el evento pasa a SOLD_OUT en la misma transacción
        if (paidTickets + 1 >= capacity) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }

        return ticketMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse findByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado: " + ticketCode));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findByUserEmail(String email) {
        return ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(email)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        return ticketRepository.findByEventEventCodeAndStatus(eventCode, TicketStatus.PAID)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    // BR-TICKET-010..012: solo tickets PAID, y no después de la fecha del evento
    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado: " + ticketCode));

        // BR-TICKET-010 y BR-TICKET-011: solo se cancela un ticket PAID (no USED ni CANCELLED)
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Solo se pueden cancelar tickets en estado PAID");
        }
        // BR-TICKET-012: no cancelar después de la fecha del evento
        LocalDateTime eventDate = ticket.getEvent().getEventDate();
        if (eventDate != null && !eventDate.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("No se puede cancelar un ticket después de la fecha del evento");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket updated = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updated);
    }

    // BR-TICKET-013 y BR-TICKET-014: solo tickets PAID pueden pasar a USED
    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado: " + ticketCode));

        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Solo se puede marcar como usado un ticket en estado PAID");
        }

        ticket.setStatus(TicketStatus.USED);
        Ticket updated = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updated);
    }

    // BR-TICKET-006: la edad se calcula con UserProfile.birthDate evaluada en la fecha del evento
    private void validateMinimumAge(User user, Event event) {
        Integer minimumAge = event.getMinimumAge();
        if (minimumAge == null || minimumAge <= 0) {
            return; // 0 significa sin restricción de edad
        }
        if (user.getProfile() == null || user.getProfile().getBirthDate() == null) {
            throw new BusinessRuleException("El usuario no tiene fecha de nacimiento registrada para validar la edad mínima");
        }
        int age = Period.between(user.getProfile().getBirthDate(), event.getEventDate().toLocalDate()).getYears();
        if (age < minimumAge) {
            throw new BusinessRuleException("El usuario no cumple la edad mínima requerida (" + minimumAge + ")");
        }
    }

    // BR-TICKET-009: estrategia de precio encapsulada; nunca price < 0
    private BigDecimal calculatePrice(TicketType type) {
        BigDecimal multiplier = switch (type) {
            case GENERAL -> BigDecimal.ONE;            // precio base
            case STUDENT -> new BigDecimal("0.60");    // descuento
            case VIP -> new BigDecimal("2.40");        // multiplicador
            case BACKSTAGE -> new BigDecimal("4.00");  // multiplicador superior
        };
        BigDecimal price = PRECIO_BASE.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        if (price.signum() < 0) {
            throw new BusinessRuleException("El precio del ticket no puede ser negativo");
        }
        return price;
    }

    private String generateTicketCode() {
        return "TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
