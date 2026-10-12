package com.pulsepass.controller;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.EventService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    private EventResponse sampleEvent() {
        return new EventResponse(1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(30), 18,
                "VEN-SMR-01", "Marina Convention Center", List.of());
    }

    // TEST-CTRL-EVT-001: crear evento válido -> 201 Created
    @Test
    void create_ValidRequest_Returns201() throws Exception {
        when(eventService.create(any(CreateEventRequest.class))).thenReturn(sampleEvent());

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventCode":"CMF-2026","name":"Caribbean Music Fest 2026",
                                 "description":"Festival","category":"MUSIC",
                                 "eventDate":"2026-12-01T20:00:00","minimumAge":18,
                                 "venueCode":"VEN-SMR-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(eventService).create(any(CreateEventRequest.class));
    }

    // TEST-CTRL-EVT-002: request inválido por Bean Validation -> 400 sin invocar el Service (QT-CTRL-007)
    @Test
    void create_InvalidRequest_Returns400_AndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Festival","category":"MUSIC",
                                 "eventDate":"2026-12-01T20:00:00","minimumAge":18}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.eventCode").exists())
                .andExpect(jsonPath("$.details.venueCode").exists());

        verify(eventService, never()).create(any(CreateEventRequest.class));
    }

    // TEST-CTRL-EVT-003: consultar evento existente -> 200
    @Test
    void findByCode_Existing_Returns200() throws Exception {
        when(eventService.findByCode("CMF-2026")).thenReturn(sampleEvent());

        mockMvc.perform(get("/api/events/CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(eventService).findByCode("CMF-2026");
    }

    // TEST-CTRL-EVT-004: evento inexistente -> 404
    @Test
    void findByCode_Missing_Returns404() throws Exception {
        when(eventService.findByCode("NOPE"))
                .thenThrow(new ResourceNotFoundException("Evento no encontrado: NOPE"));

        mockMvc.perform(get("/api/events/NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(eventService).findByCode("NOPE");
    }

    // TEST-CTRL-EVT-005: listar publicados -> 200
    @Test
    void findPublishedEvents_Returns200() throws Exception {
        when(eventService.findPublishedEvents()).thenReturn(List.of(
                new EventSummaryResponse(1L, "CMF-2026", "Caribbean Music Fest 2026",
                        EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(30), "VEN-SMR-01")));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(eventService).findPublishedEvents();
    }

    // TEST-CTRL-EVT-006: publicar evento válido -> 200
    @Test
    void publish_Valid_Returns200() throws Exception {
        when(eventService.publish("CMF-2026")).thenReturn(sampleEvent());

        mockMvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish("CMF-2026");
    }

    // TEST-CTRL-EVT-007: publicar inválido por regla de negocio -> 409
    @Test
    void publish_Invalid_Returns409() throws Exception {
        when(eventService.publish("CMF-2026"))
                .thenThrow(new BusinessRuleException("Solo se pueden publicar eventos en estado DRAFT"));

        mockMvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));

        verify(eventService).publish("CMF-2026");
    }

    // TEST-CTRL-EVT-008: asociar artista -> 200
    @Test
    void addArtist_Valid_Returns200() throws Exception {
        when(eventService.addArtist("CMF-2026", 1L)).thenReturn(sampleEvent());

        mockMvc.perform(post("/api/events/CMF-2026/artists/1"))
                .andExpect(status().isOk());

        verify(eventService).addArtist("CMF-2026", 1L);
    }

    // TEST-CTRL-EVT-009: buscar por artista -> 200
    @Test
    void findByArtist_Returns200() throws Exception {
        when(eventService.findByArtist("Solar Beat")).thenReturn(List.of(
                new EventSummaryResponse(1L, "CMF-2026", "Caribbean Music Fest 2026",
                        EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(30), "VEN-SMR-01")));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(eventService).findByArtist("Solar Beat");
    }
}
