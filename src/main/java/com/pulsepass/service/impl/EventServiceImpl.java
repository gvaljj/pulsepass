package com.pulsepass.service.impl;

import com.pulsepass.domain.model.Event;
import com.pulsepass.dto.EventRequest;
import com.pulsepass.dto.EventResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toResponse)
                .toList(); // devuelve una lista inmodificable
    }

    @Override
    @Transactional(readOnly = true) // Nota: Asegúrate de escribir readOnly = true
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));
        return eventMapper.toResponse(event);
    }

    @Override
    public EventResponse createEvent(EventRequest request) {
        if (eventRepository.existsByEventCode(request.eventCode())) {
            throw new DuplicateResourceException("Ya existe un evento con el código: " + request.eventCode());
        }
        Event event = eventMapper.toEntity(request);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponse(savedEvent);
    }

    @Override
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con ID: " + id));
        
        eventMapper.updateEntityFromRequest(request, event);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Evento no encontrado con ID: " + id);
        }
        eventRepository.deleteById(id);
    }
}