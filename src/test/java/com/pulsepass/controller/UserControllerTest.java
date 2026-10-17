package com.pulsepass.controller;

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.security.SecurityTestConfiguration;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.UserService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PRD §17.4: cadena de seguridad REAL en la slice (sin addFilters=false) + identidad explícita por petición
// con .with(user(...)) (spring-security-test).
// FR-AUTH-001: el registro es PÚBLICO; las consultas de usuario exigen autenticación (propiedad en el Service).
@WebMvcTest(UserController.class)
@Import(SecurityTestConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // Fase 4: el registro delega en AuthService (atomicidad usuario + hash + rol, PRD §9.3)
    @MockitoBean
    private AuthService authService;

    private UserResponse sampleUser() {
        return new UserResponse(1L, "andrea", "andrea@email.com", "Andrea", "Gomez",
                "300", "Santa Marta", LocalDate.of(2001, 1, 1), true);
    }

    private String validBody() {
        return """
                {"username":"andrea","email":"andrea@email.com","firstName":"Andrea",
                 "lastName":"Gomez","phone":"300","city":"Santa Marta","birthDate":"2001-01-01",
                 "password":"Segura#2026x"}
                """;
    }

    // TEST-CTRL-USR-001: registro válido -> 201 Created (ruta pública, sin identidad)
    @Test
    @WithAnonymousUser
    void register_Valid_Returns201() throws Exception {
        when(authService.register(any(RegisterUserRequest.class))).thenReturn(sampleUser());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(authService).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-002: email inválido -> 400 sin invocar el AuthService
    @Test
    void register_InvalidEmail_Returns400_AndDoesNotCallService() throws Exception {
        String invalidEmail = """
                {"username":"andrea","email":"no-es-email","firstName":"Andrea",
                 "lastName":"Gomez","birthDate":"2001-01-01","password":"Segura#2026x"}
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());

        verify(authService, never()).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-002b / §9.3: contraseña corta -> 400 con detalle del campo password
    @Test
    void register_WeakPassword_Returns400_AndDoesNotCallService() throws Exception {
        String weakPassword = """
                {"username":"andrea","email":"andrea@email.com","firstName":"Andrea",
                 "lastName":"Gomez","birthDate":"2001-01-01","password":"corta"}
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weakPassword))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());

        verify(authService, never()).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-002c / §9.3: el máximo de contraseña son 72 BYTES. 37 "á" (74 bytes UTF-8)
    // cumplen el mínimo de 12 caracteres pero exceden el límite de BCrypt -> 400.
    // En el JSON se usa la secuencia de escape \u00e1 para no depender del charset del request.
    @Test
    void register_PasswordExceedingByteLimit_Returns400_AndDoesNotCallService() throws Exception {
        String body = """
                {"username":"andrea","email":"andrea@email.com","firstName":"Andrea",
                 "lastName":"Gomez","birthDate":"2001-01-01","password":"%s"}
                """.formatted("\\u00e1".repeat(37));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());

        verify(authService, never()).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-002d / §9.3: exactamente 72 bytes (72 ASCII) es el borde superior válido -> 201.
    @Test
    @WithAnonymousUser
    void register_PasswordAtByteLimit_Returns201() throws Exception {
        when(authService.register(any(RegisterUserRequest.class))).thenReturn(sampleUser());

        String body = """
                {"username":"andrea","email":"andrea@email.com","firstName":"Andrea",
                 "lastName":"Gomez","birthDate":"2001-01-01","password":"%s"}
                """.formatted("A".repeat(72));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(authService).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-003: username duplicado -> 409
    @Test
    void register_Duplicate_Returns409() throws Exception {
        when(authService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("El username ya existe: andrea"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(authService).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-004: consultar por email -> 200 (con identidad; la propiedad la valida el Service)
    @Test
    void findByEmail_Returns200() throws Exception {
        when(userService.findByEmail("andrea@email.com")).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com")
                        .with(user("andrea").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@email.com"));

        verify(userService).findByEmail("andrea@email.com");
    }

    // TEST-CTRL-USR-004b / PRD §9.3: sin identidad -> 401 y el Service nunca se invoca
    @Test
    @WithAnonymousUser
    void findByEmail_SinIdentidad_Returns401_YNoInvocaService() throws Exception {
        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verify(userService, never()).findByEmail(any());
    }

    // TEST-CTRL-USR-005: consultar por username -> 200 (con identidad; la propiedad la valida el Service)
    @Test
    void findByUsername_Returns200() throws Exception {
        when(userService.findByUsername("andrea")).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/by-username").param("username", "andrea")
                        .with(user("andrea").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).findByUsername("andrea");
    }
}
