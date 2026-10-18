package com.pulsepass.service;

import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.domain.model.UserRole;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.SecurityErrorMessages;
import com.pulsepass.security.SecurityPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// PRD §9.1-9.4: registro atómico (SEC-004) y login con 401 uniforme (AUTH-004).
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCredentialRepository userCredentialRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterUserRequest registerRequest() {
        return new RegisterUserRequest("andrea", "andrea@email.com", "Andrea", "Gomez",
                "300", "Santa Marta", LocalDate.of(2001, 1, 1), "Segura#2026x");
    }

    private UserResponse userResponse() {
        return new UserResponse(3L, "andrea", "andrea@email.com", "Andrea", "Gomez",
                "300", "Santa Marta", LocalDate.of(2001, 1, 1), true);
    }

    private SecurityPrincipal principal() {
        return new SecurityPrincipal(3L, "andrea", "andrea@email.com", "$2a$10$hash", true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("register guarda el hash BCrypt y el rol USER en la MISMA transacción (SEC-004, sin contraseña en claro)")
    void register_GuardaHashBCrypt_YRolUser() throws Exception {
        User user = new User("andrea", "andrea@email.com");
        user.setId(3L);
        when(userService.register(any(RegisterUserRequest.class))).thenReturn(userResponse());
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Segura#2026x")).thenReturn("$2a$10$encodedhash");

        UserResponse result = authService.register(registerRequest());

        verify(passwordEncoder).encode("Segura#2026x");

        ArgumentCaptor<UserCredential> credentialCaptor = ArgumentCaptor.forClass(UserCredential.class);
        verify(userCredentialRepository).save(credentialCaptor.capture());
        assertThat(credentialCaptor.getValue().getPasswordHash()).isEqualTo("$2a$10$encodedhash");
        assertThat(credentialCaptor.getValue().getPasswordHash()).doesNotContain("Segura#2026x");
        assertThat(credentialCaptor.getValue().getUser().getId()).isEqualTo(3L);

        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRole()).isEqualTo("USER");
        assertThat(roleCaptor.getValue().getUserId()).isEqualTo(3L);

        assertThat(result).isEqualTo(userResponse());
    }

    @Test
    @DisplayName("register es @Transactional: usuario, hash y rol se crean o se revierten juntos (SEC-004)")
    void register_EstaAnotadoConTransactional() throws NoSuchMethodException {
        Transactional transactional = AuthService.class
                .getMethod("register", RegisterUserRequest.class)
                .getAnnotation(Transactional.class);

        assertThat(transactional).as("atomicidad del registro (PRD §9.1/SEC-004)").isNotNull();
    }

    @Test
    @DisplayName("login correcto: devuelve access token Bearer con el TTL del sistema (900 s)")
    void login_Correcto_RetornaTokenConTtl() {
        SecurityPrincipal principal = principal();
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(jwtService.generateToken(principal)).thenReturn("jwt-token");
        when(jwtService.getTokenTtl()).thenReturn(Duration.ofSeconds(900));

        AuthResponse response = authService.login(new LoginRequest("andrea", "Segura#2026x"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900);
    }

    @Test
    @DisplayName("login fallido -> SIEMPRE el mismo BadCredentialsException genérico (AUTH-004, sin revelar la causa)")
    void login_Fallido_Lanza401Uniforme_SinRevelarCausa() {
        List<RuntimeException> causes = List.of(
                new BadCredentialsException("contraseña incorrecta"),
                new DisabledException("cuenta desactivada"),
                new UsernameNotFoundException("usuario no encontrado"));

        for (RuntimeException cause : causes) {
            // doThrow en vez de thenThrow: se re-define el stub en cada iteración sin disparar el previo
            org.mockito.Mockito.doThrow(cause).when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authService.login(new LoginRequest("andrea", "otra-contraseña")))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage(SecurityErrorMessages.INVALID_CREDENTIALS);
        }
    }

    @Test
    @DisplayName("login con una identidad que no es de la aplicación -> 401 genérico")
    void login_PrincipalNoDeLaAplicacion_Lanza401() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("identidad-ajena", null, List.of()));

        assertThatThrownBy(() -> authService.login(new LoginRequest("andrea", "Segura#2026x")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(SecurityErrorMessages.INVALID_CREDENTIALS);
    }
}
