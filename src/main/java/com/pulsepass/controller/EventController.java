package com.pulsepass.controller;

import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.service.EventService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// PRD §8.2: EventController delega en EventService; no reimplementa reglas de negocio (CTRL-001, CTRL-006).
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // FR-CTRL-EVT-001: crear evento -> 201 Created
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        EventResponse created = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // FR-CTRL-EVT-005: asociar artista existente -> 200 o 409
    @PostMapping("/{eventCode}/artists/{artistId}")
    public ResponseEntity<EventResponse> addArtist(@PathVariable String eventCode,
                                                   @PathVariable Long artistId) {
        return ResponseEntity.ok(eventService.addArtist(eventCode, artistId));
    }

    // FR-CTRL-EVT-003: eventos publicados (camino literal con prioridad sobre {eventCode})
    @GetMapping("/published")
    public ResponseEntity<List<EventSummaryResponse>> findPublishedEvents() {
        return ResponseEntity.ok(eventService.findPublishedEvents());
    }

    // FR-CTRL-EVT-006: eventos por nombre artístico (camino literal con prioridad sobre {eventCode})
    @GetMapping("/by-artist")
    public ResponseEntity<List<EventSummaryResponse>> findByArtist(@RequestParam String stageName) {
        return ResponseEntity.ok(eventService.findByArtist(stageName));
    }

    // FR-CTRL-EVT-002: evento por código -> 200 o 404
    @GetMapping("/{eventCode}")
    public ResponseEntity<EventResponse> findByCode(@PathVariable String eventCode) {
        return ResponseEntity.ok(eventService.findByCode(eventCode));
    }

    // FR-CTRL-EVT-004: publicar evento -> 200 o 409
    @PatchMapping("/{eventCode}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable String eventCode) {
        return ResponseEntity.ok(eventService.publish(eventCode));
    }
}
