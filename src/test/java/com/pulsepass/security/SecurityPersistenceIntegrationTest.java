package com.pulsepass.security;

import com.pulsepass.TestcontainersConfiguration;
import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.domain.model.UserRole;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PRD §9.1/SEC-004: la migración V5 es incremental (V1-V4 intactas), las credenciales se persisten SOLO
// como hash BCrypt y el registro es atómico (sin filas huérfanas si algo falla).
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties =
        "pulsepass.security.jwt.secret-base64=" + SecurityTestConfiguration.TEST_SECRET_BASE64)
@DisplayName("Persistencia de seguridad: Flyway V5, BCrypt y registro atómico")
class SecurityPersistenceIntegrationTest {

    private static final String PASSWORD = "Segura#2026x";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserCredentialRepository userCredentialRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

    private String registerBody(String username, String email, String password) {
        return """
                {"username":"%s","email":"%s","firstName":"Nombre","lastName":"Apellido",
                 "birthDate":"1996-01-01","password":"%s"}
                """.formatted(username, email, password);
    }

    private ResultActions register(String username, String email, String password) throws Exception {
        return mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, email, password)));
    }

    private User seedUserWithCredential(String username, String email) {
        User user = new User(username, email);
        user.setActive(true);
        user = userRepository.save(user);
        userCredentialRepository.save(new UserCredential(user, passwordEncoder.encode(PASSWORD)));
        userRoleRepository.save(new UserRole(user, "USER"));
        return user;
    }

    private String loginBody(String identifier, String password) {
        return """
                {"identifier":"%s","password":"%s"}
                """.formatted(identifier, password);
    }

    @Test
    @DisplayName("Flyway: V1-V5 aplicadas con éxito (V5 incremental, sin tocar V1-V4)")
    void flyway_V1AV5Aplicadas() {
        List<Map<String, Object>> history = jdbcTemplate.queryForList(
                "SELECT version, success FROM flyway_schema_history ORDER BY installed_rank");

        assertThat(history).isNotEmpty();
        assertThat(history)
                .extracting(row -> String.valueOf(row.get("version")))
                .contains("1", "2", "3", "4", "5");
        assertThat(history)
                .extracting(row -> row.get("success"))
                .containsOnly(Boolean.TRUE);

        // Las tablas nuevas de V5 existen y son consultables
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_credentials", Long.class))
                .isNotNull();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_roles", Long.class))
                .isNotNull();
    }

    @Test
    @DisplayName("El registro persiste SOLO el hash BCrypt (nunca la contraseña en claro) y el rol USER")
    void registro_PersisteSoloHashBCrypt_YRolUser() throws Exception {
        register("persistencia", "persistencia@email.com", PASSWORD)
                .andExpect(status().isCreated());

        User user = userRepository.findByUsername("persistencia").orElseThrow();
        UserCredential credential = userCredentialRepository.findByUserId(user.getId()).orElseThrow();

        assertThat(credential.getPasswordHash())
                .startsWith("$2")
                .isNotEqualTo(PASSWORD)
                .doesNotContain(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, credential.getPasswordHash())).isTrue();

        assertThat(userRoleRepository.findByUserId(user.getId()))
                .extracting(UserRole::getRole)
                .containsExactly("USER");
    }

    @Test
    @DisplayName("Registro atómico (SEC-004): un fallo por duplicado deja 0 filas huérfanas")
    void registro_Fallido_NoDejaFilasHuerfanas() throws Exception {
        register("atomico", "atomico@email.com", PASSWORD).andExpect(status().isCreated());

        long users = userRepository.count();
        long credentials = userCredentialRepository.count();
        long roles = userRoleRepository.count();

        // Username duplicado -> 409 sin crear nada
        register("atomico", "otro-atomico@email.com", PASSWORD)
                .andExpect(status().isConflict());

        // Username libre pero email duplicado -> 409 sin crear nada
        register("atomico2", "atomico@email.com", PASSWORD)
                .andExpect(status().isConflict());

        assertThat(userRepository.count()).isEqualTo(users);
        assertThat(userCredentialRepository.count()).isEqualTo(credentials);
        assertThat(userRoleRepository.count()).isEqualTo(roles);
        assertThat(userRepository.findByUsername("atomico2")).isEmpty();
    }

    @Test
    @DisplayName("Usuario legado sin credencial en V5: NO puede iniciar sesión (deny-by-default)")
    void usuarioLegadoSinCredencial_NoPuedeLogin() throws Exception {
        User legacy = new User("legado", "legado@email.com");
        legacy.setActive(true);
        legacy = userRepository.save(legacy);
        userRoleRepository.save(new UserRole(legacy, "USER"));
        // Sin fila en user_credentials a propósito

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("legado", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(SecurityErrorMessages.INVALID_CREDENTIALS));

        assertThat(userCredentialRepository.findByUserId(legacy.getId())).isEmpty();
    }

    @Test
    @DisplayName("Procedimiento V5 (§9.2): activa un usuario preexistente sin password por defecto y rechaza reactivaciones")
    void procedimientoV5_ActivaUsuarioPreexistente() throws Exception {
        User legacy = new User("legado-activo", "legado-activo@email.com");
        legacy.setActive(true);
        legacy = userRepository.save(legacy);
        Long legacyId = legacy.getId();
        // Sin credencial ni rol todavía: deny-by-default hasta la activación segura (§9.2)

        // El hash BCrypt lo aporta el proceso de activación (contraseña elegida por el propio usuario);
        // la función únicamente valida y enlaza: nunca se genera ni comparte un password predeterminado.
        String hash = passwordEncoder.encode(PASSWORD);
        Boolean activada = jdbcTemplate.queryForObject(
                "SELECT assign_user_credential(?, ?, 'USER')", Boolean.class, legacyId, hash);

        assertThat(activada).isTrue();
        assertThat(passwordEncoder.matches(PASSWORD,
                userCredentialRepository.findByUserId(legacyId).orElseThrow().getPasswordHash())).isTrue();
        assertThat(userRoleRepository.findByUserId(legacyId))
                .extracting(UserRole::getRole)
                .contains("USER");

        // Tras la activación, el usuario legado ya puede autenticarse
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("legado-activo", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // Segunda vinculación -> rechazada: jamás se sobrescribe una credencial existente
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "SELECT assign_user_credential(?, ?, 'USER')", Boolean.class, legacyId, hash))
                .hasStackTraceContaining("ya tiene credencial asignada");

        // Hash que no es BCrypt -> rechazado (nunca se admite un password en claro ni uno compartido)
        User otro = new User("legado-rechazado", "legado-rechazado@email.com");
        otro.setActive(true);
        otro = userRepository.save(otro);
        Long otroId = otro.getId();
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "SELECT assign_user_credential(?, ?, 'USER')", Boolean.class, otroId, "password-en-claro"))
                .hasStackTraceContaining("no es BCrypt");

        // Usuario inexistente -> rechazado
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "SELECT assign_user_credential(?, ?, 'USER')", Boolean.class, 999_999L, hash))
                .hasStackTraceContaining("no existe");
    }

    @Test
    @DisplayName("Login con contraseña incorrecta: 401 uniforme que no filtra el hash BCrypt")
    void login_ContraseñaIncorrecta_401_SinFiltrarHash() throws Exception {
        seedUserWithCredential("hashcheck", "hashcheck@email.com");

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("hashcheck", "contraseñaintroducidaporelusuario")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(SecurityErrorMessages.INVALID_CREDENTIALS))
                .andReturn().getResponse().getContentAsString();

        assertThat(body)
                .doesNotContain("$2a$")
                .doesNotContain("contraseñaintroducidaporelusuario")
                .doesNotContain("Exception");
    }

    @Test
    @DisplayName("Login correcto contra la BD: 200 con token Bearer y sin reflejar la contraseña")
    void login_Correcto_ContraBaseDeDatos_RetornaToken() throws Exception {
        seedUserWithCredential("bdcheck", "bdcheck@email.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("bdcheck", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    @DisplayName("El usuario registrado por HTTP puede usar el token para consultar /api/auth/me")
    void registroYTokenFuncionanContraLaBaseDeDatos() throws Exception {
        register("flux", "flux@email.com", PASSWORD).andExpect(status().isCreated());

        String token = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("flux", PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("flux"))
                .andExpect(jsonPath("$.email").value("flux@email.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    @DisplayName("La fecha de nacimiento del perfil se persiste junto al usuario (contrato de registro)")
    void registro_PersistePerfil() throws Exception {
        register("perfilado", "perfilado@email.com", PASSWORD).andExpect(status().isCreated());

        User user = userRepository.findByUsername("perfilado").orElseThrow();
        LocalDate birthDate = jdbcTemplate.queryForObject(
                "SELECT birth_date FROM user_profiles WHERE user_id = ?", LocalDate.class, user.getId());
        assertThat(birthDate).isEqualTo(LocalDate.of(1996, 1, 1));
    }

    @Test
    @DisplayName("V5 §17.3: la PK compuesta (user_id, role) rechaza un rol duplicado")
    void userRole_RolDuplicado_RechazadoPorPkCompuesta() {
        User user = userRepository.saveAndFlush(new User("role_dup", "role_dup@email.com"));
        String insertRole = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";

        // JDBC directo: con @IdClass, saveAndFlush podría hacer merge y ocultar el INSERT duplicado.
        jdbcTemplate.update(insertRole, user.getId(), "USER");

        assertThatThrownBy(() -> jdbcTemplate.update(insertRole, user.getId(), "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("V5 §17.3: el CHECK chk_role_value rechaza roles fuera de USER/STAFF/ADMIN")
    void userRole_RolInvalido_RechazadoPorCheck() {
        User user = userRepository.saveAndFlush(new User("role_invalid", "role_invalid@email.com"));

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role) VALUES (?, ?)", user.getId(), "SUPERADMIN"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
