package com.pulsepass.controller;

import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserRole;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import com.pulsepass.security.SecurityPrincipal;
import com.pulsepass.security.SecurityTestConfiguration;
import com.pulsepass.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PRD §9.2: login público con 401 genérico (AUTH-004) e identidad del token en /api/auth/me (FR-AUTH-004).
@WebMvcTest(AuthController.class)
@Import(SecurityTestConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.pulsepass.security.JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository userCredentialRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @MockitoBean
    private AuthService authService;

    private String validLoginBody() {
        return """
                {"identifier":"andrea","password":"Segura#2026x"}
                """;
    }

    // Prepara la identidad de "andrea" (id 7, rol USER) en los repositorios mockeados de la slice
    // y devuelve un token real emitido con el JwtService de la configuración de pruebas.
    private String tokenForAndrea() {
        User user = new User("andrea", "andrea@email.com");
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userCredentialRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(userRoleRepository.findByUserId(7L)).thenReturn(List.of(new UserRole(user, "USER")));

        return jwtService.generateToken(new SecurityPrincipal(7L, "andrea", "andrea@email.com",
                null, true, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    // FR-AUTH-002/003: login correcto -> 200 con access token, tipo Bearer y TTL (T-SEC-005)
    @Test
    void login_Correcto_Retorna200_ConToken() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("jwt-token", "Bearer", 900));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    // AUTH-004: cualquier fallo produce el MISMO 401 con mensaje genérico (T-SEC-006)
    @Test
    void login_Fallido_Retorna401Generico() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    // §9.3/§11.2: campos obligatorios -> 400 con detalle de validación (T-SEC-018)
    @Test
    void login_SinCampos_Retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.identifier").exists())
                .andExpect(jsonPath("$.details.password").exists());
    }

    // FR-AUTH-004: /me sin token -> 401 genérico (T-SEC-001)
    @Test
    @WithAnonymousUser
    void me_SinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // FR-AUTH-004: /me con token válido -> 200 con la identidad vigente de la base de datos
    @Test
    void me_ConTokenValido_RetornaIdentidad() throws Exception {
        String token = tokenForAndrea();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@email.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.active").value(true));
    }

    // T-SEC-007: token expirado -> 401 genérico
    @Test
    void me_TokenExpirado_Retorna401() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(SecurityTestConfiguration.TEST_SECRET_BASE64));
        Instant past = Instant.now().minus(Duration.ofHours(1));
        String expired = Jwts.builder()
                .subject("7")
                .issuer("pulsepass-api")
                .issuedAt(Date.from(past))
                .expiration(Date.from(past.plus(Duration.ofMinutes(30))))
                .id("jti-expirado")
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
