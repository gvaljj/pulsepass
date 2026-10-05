package com.pulsepass.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.dto.EventRequest;
import com.pulsepass.dto.EventResponse;
import com.pulsepass.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @Test
    void getAllEvents_ShouldReturnList() throws Exception {
        EventResponse response = new EventResponse(
            1L, "EVT-001", "Concierto Rock", "Un gran concierto",
            EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 18, null
        );

        when(eventService.getAllEvents()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventCode").value("EVT-001"))
                .andExpect(jsonPath("$[0].name").value("Concierto Rock"));
    }

    @Test
    void createEvent_WhenValid_ShouldReturnCreated() throws Exception {
        EventRequest request = new EventRequest(
            "EVT-001", "Concierto Rock", "Un gran concierto",
            EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 18, null
        );

        EventResponse response = new EventResponse(
            1L, "EVT-001", "Concierto Rock", "Un gran concierto",
            EventCategory.MUSIC, EventStatus.PUBLISHED, request.eventDate(), 18, null
        );

        when(eventService.createEvent(any(EventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventCode").value("EVT-001"));
    }

    @Test
    void deleteEvent_ShouldReturnNoContent() throws Exception {
        doNothing().when(eventService).deleteEvent(1L);

        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isNoContent());
    }
}