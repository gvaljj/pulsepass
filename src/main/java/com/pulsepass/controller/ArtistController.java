package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.service.ArtistService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// PRD §8.3: ArtistController delega en ArtistService (CTRL-001, CTRL-009).
@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    // FR-CTRL-ART-003: artistas activos (camino literal con prioridad sobre {id})
    @GetMapping("/active")
    public ResponseEntity<List<ArtistResponse>> findActiveArtists() {
        return ResponseEntity.ok(artistService.findActiveArtists());
    }

    // FR-CTRL-ART-002: artista por nombre artístico -> 200 o 404
    @GetMapping("/by-stage-name")
    public ResponseEntity<ArtistResponse> findByStageName(@RequestParam String stageName) {
        return ResponseEntity.ok(artistService.findByStageName(stageName));
    }

    // FR-CTRL-ART-001: artista por ID -> 200 o 404
    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.findById(id));
    }
}
