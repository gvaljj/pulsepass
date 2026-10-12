package com.pulsepass.controller;

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.service.UserService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserResponse sampleUser() {
        return new UserResponse(1L, "andrea", "andrea@email.com", "Andrea", "Gomez",
                "300", "Santa Marta", LocalDate.of(2001, 1, 1), true);
    }

    private String validBody() {
        return """
                {"username":"andrea","email":"andrea@email.com","firstName":"Andrea",
                 "lastName":"Gomez","phone":"300","city":"Santa Marta","birthDate":"2001-01-01"}
                """;
    }

    // TEST-CTRL-USR-001: registro válido -> 201 Created
    @Test
    void register_Valid_Returns201() throws Exception {
        when(userService.register(any(RegisterUserRequest.class))).thenReturn(sampleUser());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-002: email inválido -> 400 sin invocar el Service
    @Test
    void register_InvalidEmail_Returns400_AndDoesNotCallService() throws Exception {
        String invalidEmail = """
                {"username":"andrea","email":"no-es-email","firstName":"Andrea",
                 "lastName":"Gomez","birthDate":"2001-01-01"}
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());

        verify(userService, never()).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-003: username duplicado -> 409
    @Test
    void register_Duplicate_Returns409() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("El username ya existe: andrea"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(userService).register(any(RegisterUserRequest.class));
    }

    // TEST-CTRL-USR-004: consultar por email -> 200
    @Test
    void findByEmail_Returns200() throws Exception {
        when(userService.findByEmail("andrea@email.com")).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@email.com"));

        verify(userService).findByEmail("andrea@email.com");
    }

    // TEST-CTRL-USR-005: consultar por username -> 200
    @Test
    void findByUsername_Returns200() throws Exception {
        when(userService.findByUsername("andrea")).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/by-username").param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).findByUsername("andrea");
    }
}
