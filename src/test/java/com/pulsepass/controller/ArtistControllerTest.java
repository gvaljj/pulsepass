package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.security.SecurityTestConfiguration;
import com.pulsepass.service.ArtistService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PRD §17.4: se importa la cadena de seguridad real; /api/artists/** es público (sin identidad requerida).
@WebMvcTest(ArtistController.class)
@Import(SecurityTestConfiguration.class)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService artistService;

    // TEST-CTRL-ART-001: artista por ID -> 200 OK
    @Test
    void findById_ExistingArtist_Returns200() throws Exception {
        when(artistService.findById(1L)).thenReturn(new ArtistResponse(1L, "Solar Beat", "Pop", true));

        mockMvc.perform(get("/api/artists/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(artistService).findById(1L);
    }

    // TEST-CTRL-ART-002: ID inexistente -> 404 con ErrorResponse
    @Test
    void findById_MissingArtist_Returns404() throws Exception {
        when(artistService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Artista no encontrado con id: 99"));

        mockMvc.perform(get("/api/artists/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Artista no encontrado con id: 99"));

        verify(artistService).findById(99L);
    }

    // TEST-CTRL-ART-003: artista por nombre artístico -> 200 OK
    @Test
    void findByStageName_Returns200() throws Exception {
        when(artistService.findByStageName("Solar Beat"))
                .thenReturn(new ArtistResponse(1L, "Solar Beat", "Pop", true));

        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(artistService).findByStageName("Solar Beat");
    }

    // TEST-CTRL-ART-004: artistas activos -> 200 OK
    @Test
    void findActiveArtists_Returns200() throws Exception {
        when(artistService.findActiveArtists()).thenReturn(List.of(
                new ArtistResponse(1L, "Solar Beat", "Pop", true)));

        mockMvc.perform(get("/api/artists/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stageName").value("Solar Beat"));

        verify(artistService).findActiveArtists();
    }
}
