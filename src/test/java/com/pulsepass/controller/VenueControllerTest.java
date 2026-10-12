package com.pulsepass.controller;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.VenueService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    // TEST-CTRL-VEN-001: venue existente -> 200 OK
    @Test
    void findByCode_ExistingVenue_Returns200() throws Exception {
        when(venueService.findByCode("VEN-SMR-01")).thenReturn(
                new VenueResponse(1L, "VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1", 3, true));

        mockMvc.perform(get("/api/venues/VEN-SMR-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name").value("Marina Convention Center"));

        verify(venueService).findByCode("VEN-SMR-01");
    }

    // TEST-CTRL-VEN-002: venue inexistente -> 404 con ErrorResponse
    @Test
    void findByCode_MissingVenue_Returns404() throws Exception {
        when(venueService.findByCode("NOPE"))
                .thenThrow(new ResourceNotFoundException("Venue no encontrado: NOPE"));

        mockMvc.perform(get("/api/venues/NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Venue no encontrado: NOPE"));

        verify(venueService).findByCode("NOPE");
    }

    // TEST-CTRL-VEN-003: venues activos -> 200 OK
    @Test
    void findActiveVenues_Returns200() throws Exception {
        when(venueService.findActiveVenues()).thenReturn(List.of(
                new VenueResponse(1L, "VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1", 3, true)));

        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"));

        verify(venueService).findActiveVenues();
    }
}
