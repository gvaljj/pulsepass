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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// PRD §9.1-9.4: orquesta el registro atómico (usuario + hash BCrypt + rol USER en UNA transacción)
// y el login por username o email con emisión de JWT (FR-AUTH-001..004).
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final String ROLE_USER = "USER";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserService userService,
                       UserRepository userRepository,
                       UserCredentialRepository userCredentialRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // FR-AUTH-001 / SEC-004: registro público. UserServiceImpl.register es @Transactional(REQUIRED),
    // por lo que comparte esta transacción: User + UserProfile + credencial (hash) + rol USER se crean
    // juntos o se revierten juntos. Nunca se persiste la contraseña en claro.
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        UserResponse created = userService.register(request);

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Usuario recién creado no encontrado: " + request.username()));

        // BCrypt: el hash es único por usuario (salt propio), nunca la contraseña en claro
        userCredentialRepository.save(new UserCredential(user, passwordEncoder.encode(request.password())));
        userRoleRepository.save(new UserRole(user, ROLE_USER));

        log.info("Registro de usuario completado: userId={}", user.getId());
        return created;
    }

    // FR-AUTH-002/003: login por username o email -> access token con TTL del sistema.
    // Cualquier fallo (usuario inexistente, contraseña incorrecta o cuenta inactiva) produce
    // UN MISMO 401 con mensaje genérico: no revela existencia de usuarios ni el estado de la cuenta.
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

            if (!(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
                throw new BadCredentialsException(SecurityErrorMessages.INVALID_CREDENTIALS);
            }

            String accessToken = jwtService.generateToken(principal);
            // NFR-SEC-006: se registra el resultado del login sin credenciales ni tokens
            log.info("Login correcto: userId={}", principal.getUserId());
            return new AuthResponse(accessToken, TOKEN_TYPE_BEARER, jwtService.getTokenTtl().toSeconds());
        } catch (AuthenticationException ex) {
            log.warn("Login fallido ({})", ex.getClass().getSimpleName());
            // Envuelve cualquier causa en la misma respuesta genérica (AUTH-004)
            throw new BadCredentialsException(SecurityErrorMessages.INVALID_CREDENTIALS, ex);
        }
    }
}
