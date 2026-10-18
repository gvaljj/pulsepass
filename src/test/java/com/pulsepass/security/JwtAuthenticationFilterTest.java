package com.pulsepass.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// PRD §9.2/§16: el filtro solo autentica con un Bearer válido; cualquier token inválido deja el contexto
// vacío y la cadena continúa (el AuthenticationEntryPoint responde 401 en rutas protegidas).
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET = SecurityTestConfiguration.TEST_SECRET_BASE64;
    private static final String ISSUER = "pulsepass-api";

    @Mock
    private CustomUserDetailsService userDetailsService;

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtService = new JwtService(new JwtProperties(ISSUER, TEST_SECRET, "PT15M"));
        key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_SECRET));
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private SecurityPrincipal principal(boolean active) {
        return new SecurityPrincipal(7L, "andrea", "andrea@email.com", "$2a$10$hash", active,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private void doFilter(HttpServletRequest request) throws Exception {
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    @Test
    @DisplayName("Sin cabecera Authorization: la petición continúa sin autenticación")
    void sinCabecera_NoAutentica() throws Exception {
        doFilter(new MockHttpServletRequest("GET", "/api/venues/active"));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Bearer válido: autentica con el principal reconstruido desde la base de datos (id + roles)")
    void tokenValido_AutenticaConPrincipalDeBaseDeDatos() throws Exception {
        when(userDetailsService.loadPrincipalById(7L)).thenReturn(principal(true));

        doFilter(requestWithBearer(jwtService.generateToken(principal(true))));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(SecurityPrincipal.class);
        assertThat(((SecurityPrincipal) authentication.getPrincipal()).getUserId()).isEqualTo(7L);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Token expirado: se descarta y NO establece autenticación (T-SEC-007)")
    void tokenExpirado_NoAutentica() throws Exception {
        Instant past = Instant.now().minus(Duration.ofHours(1));
        String expired = Jwts.builder()
                .subject("7")
                .issuer(ISSUER)
                .issuedAt(Date.from(past))
                .expiration(Date.from(past.plus(Duration.ofMinutes(30))))
                .id("jti-expirado")
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        doFilter(requestWithBearer(expired));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Token con firma alterada: se descarta sin establecer autenticación (T-SEC-008)")
    void tokenFirmadoMal_NoAutentica() throws Exception {
        String valid = jwtService.generateToken(principal(true));
        String tampered = valid.substring(0, valid.length() - 1) + (valid.endsWith("A") ? "B" : "A");

        doFilter(requestWithBearer(tampered));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Esquema distinto de Bearer (Basic): se ignora por completo")
    void esquemaDistintoABearer_NoSeProcesa() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        doFilter(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Doble cabecera Authorization: solicitud ambigua, no se autentica")
    void dobleCabeceraAuthorization_NoAutentica() throws Exception {
        String token = jwtService.generateToken(principal(true));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("Authorization", "Bearer " + token);

        doFilter(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Token solo en query string: nunca se usa (el token vive únicamente en el header)")
    void tokenEnQueryString_NoSeUsa() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.setParameter("access_token", jwtService.generateToken(principal(true)));

        doFilter(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Usuario desactivado en la base de datos: el token válido deja de otorgar acceso (SEC-011)")
    void usuarioDesactivado_NoAutentica() throws Exception {
        when(userDetailsService.loadPrincipalById(7L)).thenReturn(principal(false));

        doFilter(requestWithBearer(jwtService.generateToken(principal(true))));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Contexto con AnonymousAuthenticationToken (lo deja AnonymousAuthenticationFilter antes del filtro): el Bearer SÍ autentica")
    void contextoAnonimo_ElBearerSiAutentica() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.AnonymousAuthenticationToken(
                        "clave-anonima", "anonymousUser",
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        when(userDetailsService.loadPrincipalById(7L)).thenReturn(principal(true));

        doFilter(requestWithBearer(jwtService.generateToken(principal(true))));

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull()
                .isNotInstanceOf(org.springframework.security.authentication.AnonymousAuthenticationToken.class);
    }

    @Test
    @DisplayName("Una autenticación ya establecida nunca se sobrescribe")
    void autenticacionPrevia_NoSeSobrescribe() throws Exception {
        Authentication existing = new UsernamePasswordAuthenticationToken(
                "identidad-previa", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);
        // Sin stub de loadPrincipalById: el filtro ni siquiera debe intentar reconstruir la identidad

        doFilter(requestWithBearer(jwtService.generateToken(principal(true))));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
    }
}
