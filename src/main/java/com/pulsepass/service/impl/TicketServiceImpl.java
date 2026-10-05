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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pulsepass.service.TicketService;

import java.math.BigDecimal;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

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

    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {
        // BR-TICKET-001 y BR-TICKET-002: Buscar y validar usuario
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + request.userEmail()));
        if (!user.isActive()) {
            throw new BusinessRuleException("El usuario se encuentra inactivo y no puede comprar tickets");
        }

        // BR-TICKET-003, BR-TICKET-004, BR-TICKET-005: Buscar y validar evento
        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con código: " + request.eventCode()));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException("Solo se pueden comprar tickets para eventos en estado PUBLISHED");
        }

        // BR-TICKET-006: Validar edad mínima si el evento lo requiere
        if (event.getMinimumAge() != null && event.getMinimumAge() > 0) {
            if (user.getUserProfile() == null || user.getUserProfile().getBirthDate() == null) {
                throw new BusinessRuleException("El usuario no tiene fecha de registro de nacimiento para validar la edad");
            }
            int age = Period.between(user.getUserProfile().getBirthDate(), event.getEventDate().toLocalDate()).getYears();
            if (age < event.getMinimumAge()) {
                throw new BusinessRuleException("El usuario no cumple con la edad mínima requerida para este evento");
            }
        }

        // BR-TICKET-007: Validar capacidad del venue
        long paidTicketsCount = ticketRepository.countByEventEventCodeAndStatus(event.getEventCode(), TicketStatus.PAID);
        int venueCapacity = event.getVenue().getCapacity();

        if (paidTicketsCount >= venueCapacity) {
            throw new BusinessRuleException("El evento se encuentra agotado (capacidad máxima alcanzada)");
        }

        // Calcular precio según el tipo de ticket (Estrategia académica base)
        BigDecimal price = calculatePrice(request.type());

        // Crear el ticket
        Ticket ticket = new Ticket();
        ticket.setTicketCode("TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ticket.setType(request.type());
        ticket.setPrice(price);
        ticket.setStatus(TicketStatus.PAID);
        ticket.setUser(user);
        ticket.setEvent(event);

        Ticket savedTicket = ticketRepository.save(ticket);

        // BR-TICKET-008: Si esta compra completa la capacidad, cambiar evento a SOLD_OUT
        if (paidTicketsCount + 1 >= venueCapacity) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }

        return ticketMapper.toResponse(savedTicket);
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
        return ticketRepository.countByEventEventCodeAndStatus(eventCode, TicketStatus.PAID) > 0 ? 
                ticketRepository.findByEventEventCodeAndStatus(eventCode, TicketStatus.PAID)
                        .stream().map(ticketMapper::toResponse).toList() 
                : List.of();
    }

    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado: " + ticketCode));

        // BR-TICKET-010 y BR-TICKET-011: Solo se puede cancelar si está PAID
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Solo se pueden cancelar tickets que se encuentren en estado PAID");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket updated = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado: " + ticketCode));

        // BR-TICKET-013 y BR-TICKET-014: Solo PAID puede pasar a USED
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Solo se puede marcar como usado un ticket en estado PAID");
        }

        ticket.setStatus(TicketStatus.USED);
        Ticket updated = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updated);
    }

    private BigDecimal calculatePrice(TicketType type) {
        return switch (type) {
            case GENERAL -> new BigDecimal("50.00");
            case STUDENT -> new BigDecimal("30.00");
            case VIP -> new BigDecimal("120.00");
            case BACKSTAGE -> new BigDecimal("200.00");
        };
    }
}