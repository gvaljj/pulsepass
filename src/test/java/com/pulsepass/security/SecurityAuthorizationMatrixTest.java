package com.pulsepass.security;

import com.pulsepass.TestcontainersConfiguration;
import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.domain.model.Artist;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.domain.model.UserProfile;
import com.pulsepass.domain.model.UserRole;
import com.pulsepass.domain.model.Venue;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserProfileRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PRD §17: pruebas de seguridad con los FILTROS ACTIVOS (nunca addFilters=false), identidad real emitida
// por el propio JwtService contra la base de datos. Cada prueba lleva su id T-SEC-xxx del PRD §17.3.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties =
        "pulsepass.security.jwt.secret-base64=" + SecurityTestConfiguration.TEST_SECRET_BASE64)
@DisplayName("T-SEC-001..020: matriz de autorización y flujos de la capa de seguridad")
class SecurityAuthorizationMatrixTest {

    private static final String PASSWORD = "Segura#2026x";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserCredentialRepository userCredentialRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // Spies: permiten verificar "nunca se invocó" sin perder el comportamiento real (T-SEC-003/008)
    @MockitoSpyBean
    private EventService eventService;
    @MockitoSpyBean
    private TicketService ticketService;

    private User andrea;
    private User luis;
    private User pepe;
    private User miguel;
    private User juanDesactivado;
    private Artist artist;
    private Venue venue;
    private Event publishedEvent;
    private Event draftEvent;

    @BeforeEach
    void seed() {
        // Estado limpio y determinista entre pruebas (orden por claves foráneas).
        // event_artists se limpia por JDBC: la colección artists de las entidades cargadas es lazy (detached).
        jdbcTemplate.execute("DELETE FROM event_artists");
        ticketRepository.deleteAll();
        eventRepository.deleteAll();
        artistRepository.deleteAll();
        venueRepository.deleteAll();
        userCredentialRepository.deleteAll();
        userRoleRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();

        venue = venueRepository.save(new Venue("VEN-01", "Marina Convention Center", "Santa Marta", "Cra 1", 500));
        artist = artistRepository.save(new Artist("Solar Beat", "Colombia", "Electro"));

        publishedEvent = new Event();
        publishedEvent.setEventCode("CMF-2026");
        publishedEvent.setName("Caribbean Music Fest 2026");
        publishedEvent.setCategory(EventCategory.MUSIC);
        publishedEvent.setStatus(EventStatus.PUBLISHED);
        publishedEvent.setEventDate(LocalDateTime.now().plusDays(30));
        publishedEvent.setMinimumAge(18);
        publishedEvent.setVenue(venue);
        publishedEvent.getArtists().add(artist);
        eventRepository.save(publishedEvent);

        draftEvent = new Event();
        draftEvent.setEventCode("DRF-2026");
        draftEvent.setName("Borrador Secreto");
        draftEvent.setCategory(EventCategory.MUSIC);
        draftEvent.setStatus(EventStatus.DRAFT);
        draftEvent.setEventDate(LocalDateTime.now().plusDays(60));
        draftEvent.setMinimumAge(18);
        draftEvent.setVenue(venue);
        draftEvent.getArtists().add(artist);
        eventRepository.save(draftEvent);

        andrea = saveUser("andrea", "andrea@email.com", "USER", true);
        luis = saveUser("luis", "luis@email.com", "USER", true);
        pepe = saveUser("pepe", "pepe@email.com", "STAFF", true);
        miguel = saveUser("miguel", "miguel@email.com", "ADMIN", true);
        juanDesactivado = saveUser("juan", "juan@email.com", "USER", false);

        saveTicket("TCK-OWN", andrea, publishedEvent, TicketStatus.PAID);
        saveTicket("TCK-PEPE", pepe, publishedEvent, TicketStatus.PAID);
    }

    // ---------------------------------------------------------------- helpers

    private User saveUser(String username, String email, String role, boolean active) {
        User user = new User(username, email);
        user.setActive(active);
        user = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setFirstName("Nombre");
        profile.setLastName("Apellido");
        profile.setBirthDate(LocalDate.now().minusYears(30));
        profile.setUser(user);
        user.setProfile(profile);
        user = userRepository.save(user);

        userCredentialRepository.save(new UserCredential(user, passwordEncoder.encode(PASSWORD)));
        userRoleRepository.save(new UserRole(user, role));
        return user;
    }

    private Ticket saveTicket(String code, User user, Event event, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setTicketCode(code);
        ticket.setType(TicketType.GENERAL);
        ticket.setPrice(new BigDecimal("50.00"));
        ticket.setStatus(status);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);
        return ticketRepository.save(ticket);
    }

    // Token real: identidad resuelta desde la base de datos (id persistente + roles vigentes)
    private String tokenFor(String username) {
        SecurityPrincipal principal = (SecurityPrincipal) userDetailsService.loadUserByUsername(username);
        return jwtService.generateToken(principal);
    }

    private String expiredToken() {
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                java.util.Base64.getDecoder().decode(SecurityTestConfiguration.TEST_SECRET_BASE64));
        return io.jsonwebtoken.Jwts.builder()
                .subject(String.valueOf(andrea.getId()))
                .issuer("pulsepass-api")
                .issuedAt(java.util.Date.from(java.time.Instant.now().minus(java.time.Duration.ofHours(1))))
                .expiration(java.util.Date.from(java.time.Instant.now().minus(java.time.Duration.ofMinutes(30))))
                .id("jti-expirado")
                .signWith(key, io.jsonwebtoken.Jwts.SIG.HS256)
                .compact();
    }

    private String tamperedToken() {
        String valid = tokenFor("andrea");
        return valid.substring(0, valid.length() - 1) + (valid.endsWith("A") ? "B" : "A");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private ResultActions getAs(String uri, String token) throws Exception {
        return mockMvc.perform(get(uri).header("Authorization", bearer(token)));
    }

    // POST autenticado sin cuerpo (p.ej. vincular artista a evento)
    private ResultActions postAs(String uri, String token) throws Exception {
        return mockMvc.perform(post(uri).header("Authorization", bearer(token)));
    }

    private ResultActions postAs(String uri, String token, String body) throws Exception {
        return mockMvc.perform(post(uri)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions patchAs(String uri, String token) throws Exception {
        return mockMvc.perform(patch(uri).header("Authorization", bearer(token)));
    }

    // Ruta pública (permitAll): sin cabecera Authorization
    private ResultActions postPublic(String uri, String body) throws Exception {
        return mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String eventBody(String eventCode) {
        return """
                {"eventCode":"%s","name":"Festival Nuevo","description":"Desc","category":"MUSIC",
                 "eventDate":"2027-06-01T20:00:00","minimumAge":18,"venueCode":"VEN-01"}
                """.formatted(eventCode);
    }

    private String purchaseBody(String userEmail) {
        return """
                {"userEmail":"%s","eventCode":"CMF-2026","type":"GENERAL"}
                """.formatted(userEmail);
    }

    private String registerBody(String username, String email, String password) {
        return """
                {"username":"%s","email":"%s","firstName":"Nombre","lastName":"Apellido",
                 "birthDate":"1996-01-01","password":"%s"}
                """.formatted(username, email, password);
    }

    // ---------------------------------------------------------------- T-SEC-001..004: autorización

    @Test
    @DisplayName("T-SEC-001: sin token en ruta protegida -> 401 genérico, sin fuga de información")
    void tSec001_SinToken_Retorna401Generico() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(SecurityErrorMessages.UNAUTHENTICATED))
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    @DisplayName("T-SEC-002: anónimo creando evento -> 401 y el Service nunca se invoca")
    void tSec002_Anonimo_CreaEvento_Retorna401() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventBody("SEC002-2026")))
                .andExpect(status().isUnauthorized());

        verify(eventService, never()).create(any(CreateEventRequest.class));
    }

    @Test
    @DisplayName("T-SEC-003: USER creando evento -> 403, Service sin invocar y 0 eventos creados")
    void tSec003_User_CreaEvento_Retorna403() throws Exception {
        long before = eventRepository.count();

        postAs("/api/events", tokenFor("andrea"), eventBody("SEC003-2026"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        verify(eventService, never()).create(any(CreateEventRequest.class));
        assertThat(eventRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("T-SEC-004: ADMIN creando evento -> 201")
    void tSec004_Admin_CreaEvento_Retorna201() throws Exception {
        postAs("/api/events", tokenFor("miguel"), eventBody("SEC004-2026"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventCode").value("SEC004-2026"));
    }

    // ---------------------------------------------------------------- T-SEC-005..008: tokens

    @Test
    @DisplayName("T-SEC-005: login correcto -> 200 con access token, tipo Bearer y TTL 900 s")
    void tSec005_LoginCorrecto_RetornaToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"andrea","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    @DisplayName("T-SEC-006: contraseña incorrecta y usuario inexistente -> MISMO 401 genérico (sin oráculo)")
    void tSec006_LoginFallido_Mismo401() throws Exception {
        ResultActions wrongPassword = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"identifier":"andrea","password":"contraseñadefinitivamenteincorrecta"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(SecurityErrorMessages.INVALID_CREDENTIALS));

        ResultActions unknownUser = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"identifier":"no-existe","password":"contraseñadefinitivamenteincorrecta"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(SecurityErrorMessages.INVALID_CREDENTIALS));

        // Respuestas idénticas en estructura: no se distingue usuario inexistente de contraseña mala
        assertThat(wrongPassword.andReturn().getResponse().getContentAsString())
                .doesNotContain("andrea", "no-existe", "Exception");
        assertThat(unknownUser.andReturn().getResponse().getContentAsString())
                .doesNotContain("andrea", "no-existe", "Exception");
    }

    @Test
    @DisplayName("T-SEC-007: token expirado -> 401 genérico")
    void tSec007_TokenExpirado_Retorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(expiredToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("T-SEC-008: token con firma alterada -> 401 y el Service del recurso nunca se invoca")
    void tSec008_TokenFirmadoMal_Retorna401() throws Exception {
        mockMvc.perform(get("/api/tickets/TCK-OWN").header("Authorization", bearer(tamperedToken())))
                .andExpect(status().isUnauthorized());

        verify(ticketService, never()).findByCode(any());
    }

    // ---------------------------------------------------------------- T-SEC-009..012: anti-IDOR y roles

    @Test
    @DisplayName("T-SEC-009: tickets: propietario 200, usuario ajeno 403 y STAFF 200 (TKT-02)")
    void tSec009_Tickets_Propiedad() throws Exception {
        getAs("/api/tickets/TCK-OWN", tokenFor("andrea"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("TCK-OWN"));

        getAs("/api/tickets/TCK-PEPE", tokenFor("andrea"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        getAs("/api/tickets/TCK-PEPE", tokenFor("pepe"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("T-SEC-010: compra con email ajeno -> 403 y 0 tickets creados; compra propia -> 201 (TKT-01)")
    void tSec010_Compra_Propiedad() throws Exception {
        long before = ticketRepository.count();

        postAs("/api/tickets", tokenFor("andrea"), purchaseBody("luis@email.com"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        assertThat(ticketRepository.count())
                .as("una compra denegada no debe crear tickets")
                .isEqualTo(before);

        postAs("/api/tickets", tokenFor("andrea"), purchaseBody("andrea@email.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"));

        assertThat(ticketRepository.count()).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("T-SEC-011: usuario desactivado DESPUÉS de emitir el token pierde acceso en la siguiente petición")
    void tSec011_UsuarioDesactivado_PierdeAcceso() throws Exception {
        String token = tokenFor("andrea");
        getAs("/api/auth/me", token).andExpect(status().isOk());

        User user = userRepository.findByUsername("andrea").orElseThrow();
        user.setActive(false);
        userRepository.save(user);

        getAs("/api/auth/me", token)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("T-SEC-012: STAFF usa ticket y ADMIN publica; STAFF no crea eventos (matriz completa)")
    void tSec012_OperacionesPrivilegiadas() throws Exception {
        patchAs("/api/tickets/TCK-PEPE/use", tokenFor("pepe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USED"));

        patchAs("/api/events/DRF-2026/publish", tokenFor("miguel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        postAs("/api/events", tokenFor("pepe"), eventBody("SEC012-2026"))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- T-SEC-013..016: registro/login

    @Test
    @DisplayName("T-SEC-013: registro y login integrados (UC-SEC-01): 201 -> 200 -> /me con rol USER")
    void tSec013_RegistroYLoginIntegrados() throws Exception {
        postPublic("/api/users", registerBody("nuevo", "nuevo@email.com", PASSWORD))
                .andExpect(status().isCreated());

        ResultActions login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"nuevo","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        String token = login.andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");

        getAs("/api/auth/me", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nuevo"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("T-SEC-014: registro duplicado -> 409; contraseña débil -> 400 (§9.3)")
    void tSec014_Duplicado409_ContraseñaDebil400() throws Exception {
        postPublic("/api/users", registerBody("andrea", "otro@email.com", PASSWORD))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        postPublic("/api/users", registerBody("debil", "debil@email.com", "corta"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    @DisplayName("T-SEC-015: usuarios: propio 200, ajeno 403, ADMIN 200 e inexistente 404 (solo inexistencia)")
    void tSec015_Usuarios_PropiedadY404() throws Exception {
        getAs("/api/users/by-email?email=andrea@email.com", tokenFor("andrea"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrea"));

        getAs("/api/users/by-username?username=andrea", tokenFor("andrea"))
                .andExpect(status().isOk());

        getAs("/api/users/by-email?email=luis@email.com", tokenFor("andrea"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        getAs("/api/users/by-email?email=luis@email.com", tokenFor("miguel"))
                .andExpect(status().isOk());

        getAs("/api/users/by-email?email=nadie@email.com", tokenFor("andrea"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("T-SEC-016: cuenta desactivada no puede iniciar sesión; el 401 no revela el estado de la cuenta")
    void tSec016_CuentaDesactivada_NoPuedeLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"juan","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(SecurityErrorMessages.INVALID_CREDENTIALS))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("activad"))));
    }

    // ---------------------------------------------------------------- T-SEC-017..018: borradores y validación

    @Test
    @DisplayName("T-SEC-017: borradores ocultos: 404 idéntico al inexistente; publicados visibles")
    void tSec017_Borradores_Ocultos() throws Exception {
        String draftBody = mockMvc.perform(get("/api/events/DRF-2026"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        String missingBody = mockMvc.perform(get("/api/events/NOEXISTE-XYZ"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        // Misma forma de respuesta que un evento inexistente: no se revela que el borrador existe
        assertThat(draftBody).contains("\"status\":404").contains("Evento no encontrado: DRF-2026");
        assertThat(missingBody).contains("\"status\":404").contains("Evento no encontrado: NOEXISTE-XYZ");
        assertThat(draftBody).doesNotContain("Borrador Secreto", "DRAFT");

        getAs("/api/events/CMF-2026", tokenFor("andrea"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        // Búsqueda pública por artista excluye borradores; STAFF/ADMIN sí los ven
        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventCode",
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("DRF-2026"))));

        mockMvc.perform(get("/api/events/by-artist")
                        .param("stageName", "Solar Beat")
                        .header("Authorization", bearer(tokenFor("pepe"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventCode",
                        org.hamcrest.Matchers.hasItem("DRF-2026")));
    }

    @Test
    @DisplayName("T-SEC-018: validación estructural -> 400 con detalle de campos (§11.2)")
    void tSec018_ErroresDeValidacion_400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.identifier").exists())
                .andExpect(jsonPath("$.details.password").exists());

        postPublic("/api/users", registerBody("correo-malo", "no-es-correo", PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    // ---------------------------------------------------------------- T-SEC-019: sin fugas

    @Test
    @DisplayName("T-SEC-019: los errores no filtran credenciales, hashes, stack traces ni tokens")
    void tSec019_Errores_SinFugasDeInformacion() throws Exception {
        String anon401 = mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String denied403 = postAs("/api/events", tokenFor("andrea"), eventBody("SEC019-2026"))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        String invalidLogin401 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"andrea","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk()) // login válido: no debe reflejar la contraseña
                .andReturn().getResponse().getContentAsString();

        String validation400 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        for (String body : List.of(anon401, denied403, invalidLogin401, validation400)) {
            assertThat(body)
                    .doesNotContain(PASSWORD)       // nunca la contraseña en claro
                    .doesNotContain("$2a$")         // nunca el hash BCrypt
                    .doesNotContain("Exception")    // nunca clases de excepción
                    .doesNotContain("stackTrace")   // nunca stack traces
                    .doesNotContain("at com.pulsepass."); // nunca marcas de pila
        }

        // El 401 de seguridad solo anuncia el esquema, sin detalles
        String header = mockMvc.perform(get("/api/auth/me"))
                .andReturn().getResponse().getHeader("WWW-Authenticate");
        assertThat(header).isEqualTo("Bearer");
    }

    // ---------------------------------------------------------------- T-SEC-020: regresión

    @Test
    @DisplayName("T-SEC-020: regresión de la matriz completa de los 20 endpoints con sus identidades")
    void tSec020_RegresionMatrizCompleta() throws Exception {
        // ---- Públicos sin identidad
        mockMvc.perform(get("/api/venues/active")).andExpect(status().isOk());
        mockMvc.perform(get("/api/venues/VEN-01")).andExpect(status().isOk());
        mockMvc.perform(get("/api/artists/active")).andExpect(status().isOk());
        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Solar Beat")).andExpect(status().isOk());
        mockMvc.perform(get("/api/artists/" + artist.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/api/events/published")).andExpect(status().isOk());
        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat")).andExpect(status().isOk());
        mockMvc.perform(get("/api/events/CMF-2026")).andExpect(status().isOk());
        postPublic("/api/users", registerBody("regresion", "regresion@email.com", PASSWORD))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"regresion","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        String userToken = tokenFor("andrea");
        String staffToken = tokenFor("pepe");
        String adminToken = tokenFor("miguel");

        // ---- USER autenticado
        getAs("/api/auth/me", userToken).andExpect(status().isOk());
        postAs("/api/tickets", userToken, purchaseBody("andrea@email.com")).andExpect(status().isCreated());
        getAs("/api/tickets/TCK-OWN", userToken).andExpect(status().isOk());
        getAs("/api/tickets/by-user?email=andrea@email.com", userToken).andExpect(status().isOk());
        patchAs("/api/tickets/TCK-OWN/cancel", userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        // Denegaciones del USER (matriz)
        postAs("/api/events", userToken, eventBody("REG-U-2026")).andExpect(status().isForbidden());
        patchAs("/api/events/DRF-2026/publish", userToken).andExpect(status().isForbidden());
        postAs("/api/events/CMF-2026/artists/" + artist.getId(), userToken).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/events/CMF-2026/tickets/paid").header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
        patchAs("/api/tickets/TCK-PEPE/use", userToken).andExpect(status().isForbidden());
        getAs("/api/users/by-email?email=luis@email.com", userToken).andExpect(status().isForbidden());

        // ---- STAFF
        mockMvc.perform(get("/api/events/CMF-2026/tickets/paid").header("Authorization", bearer(staffToken)))
                .andExpect(status().isOk());
        patchAs("/api/tickets/TCK-PEPE/use", staffToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USED"));
        postAs("/api/events", staffToken, eventBody("REG-S-2026")).andExpect(status().isForbidden());
        // STAFF cancelando el ticket de OTRA persona -> 403: cancelar es del propietario o ADMIN (TKT-05)
        patchAs("/api/tickets/TCK-OWN/cancel", staffToken).andExpect(status().isForbidden());

        // ---- ADMIN
        postAs("/api/events", adminToken, eventBody("REG-A-2026")).andExpect(status().isCreated());
        patchAs("/api/events/DRF-2026/publish", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        // Vincular el artista al evento recién creado (a CMF-2026 ya estaba vinculado -> 409 por regla)
        postAs("/api/events/REG-A-2026/artists/" + artist.getId(), adminToken).andExpect(status().isOk());
        getAs("/api/tickets/by-user?email=luis@email.com", adminToken).andExpect(status().isOk());
    }
}
