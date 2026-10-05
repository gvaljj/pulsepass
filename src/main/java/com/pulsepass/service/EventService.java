package com.pulsepass.service;

import com.pulsepass.dto.EventRequest;
import com.pulsepass.dto.EventResponse;
import java.util.List;

public interface EventService {
    List<EventResponse> getAllEvents();
    EventResponse getEventById(Long id);
    EventResponse createEvent(EventRequest request);
    EventResponse updateEvent(Long id, EventRequest request);
    void deleteEvent(Long id);
}