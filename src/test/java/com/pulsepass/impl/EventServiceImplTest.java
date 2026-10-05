package com.pulsepass.service.impl;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Event;
import com.pulsepass.dto.EventRequest;
import com.pulsepass.dto.EventResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private EventRequest eventRequest;
    private EventResponse eventResponse;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setEventCode("EVT-001");
        event.setName("Concierto Rock");
        event.setDescription("Un gran concierto");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(10));
        event.setMinimumAge(18);

        eventRequest = new EventRequest(
            "EVT-001",
            "Concierto Rock",
            "Un gran concierto",
            EventCategory.MUSIC,
            EventStatus.PUBLISHED,
            LocalDateTime.now().plusDays(10),
            18,
            null
        );

        eventResponse = new EventResponse(
            1L,
            "EVT-001",
            "Concierto Rock",
            "Un gran concierto",
            EventCategory.MUSIC,
            EventStatus.PUBLISHED,
            event.getEventDate(),
            18,
            null
        );
    }

    @Test
    void getAllEvents_ShouldReturnListOfEvents() {
        when(eventRepository.findAll()).thenReturn(List.of(event));
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        List<EventResponse> result = eventService.getAllEvents();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getEventById_WhenExists_ShouldReturnEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals("EVT-001", result.eventCode());
    }

    @Test
    void getEventById_WhenNotExists_ShouldThrowException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(99L));
    }

    @Test
    void createEvent_WhenUniqueCode_ShouldSaveAndReturn() {
        when(eventRepository.existsByEventCode("EVT-001")).thenReturn(false);
        when(eventMapper.toEntity(eventRequest)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventService.createEvent(eventRequest);

        assertNotNull(result);
        assertEquals("EVT-001", result.eventCode());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_WhenDuplicateCode_ShouldThrowException() {
        when(eventRepository.existsByEventCode("EVT-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> eventService.createEvent(eventRequest));
        verify(eventRepository, never()).save(any(Event.class));
    }
}