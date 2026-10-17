package com.pulsepass.controller;

import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.security.SecurityErrorMessages;
import com.pulsepass.security.SecurityPrincipal;
import com.pulsepass.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// PRD §9.2: endpoints de autenticación. El login es público (permitAll) y /api/auth/me siempre
// exige un token válido; los errores los traduce el GlobalExceptionHandler (401 genérico).
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String ROLE_PREFIX = "ROLE_";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // FR-AUTH-002/003: login por username o email -> 200 con access token, o 401 genérico
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // FR-AUTH-004: identidad del token actual -> 200 con roles vigentes de la base de datos
    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            // Defensa: sin principal de la aplicación no hay identidad que reportar
            throw new BadCredentialsException(SecurityErrorMessages.UNAUTHENTICATED);
        }
        java.util.List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith(ROLE_PREFIX)
                        ? authority.substring(ROLE_PREFIX.length())
                        : authority)
                .toList();
        return ResponseEntity.ok(new AuthenticatedUserResponse(
                principal.getUserId(),
                principal.getUsername(),
                principal.getEmail(),
                roles,
                principal.isActive()));
    }
}
