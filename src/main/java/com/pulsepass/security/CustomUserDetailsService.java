package com.pulsepass.security;

import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.domain.model.UserRole;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

// PRD §9.2: resuelve la identidad desde la base de datos por username O email.
// Los roles se leen en cada autenticación/petición (roles vigentes, sin caché desactualizada).
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    UserCredentialRepository userCredentialRepository,
                                    UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userRoleRepository = userRoleRepository;
    }

    // Login: identifier puede ser username o email (FR-AUTH-002). Si no existe, UsernameNotFoundException
    // la traduce DaoAuthenticationProvider a BadCredentials -> 401 genérico sin revelar existencia del usuario.
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.isBlank()) {
            throw new UsernameNotFoundException("Identificador de usuario no proporcionado");
        }
        String trimmed = identifier.trim();
        User user = findByUsernameOrEmail(trimmed)
                .orElseThrow(() -> {
                    // No registrar el identificador en claro como si fuera una contraseña: solo la causa
                    log.debug("Autenticación fallida: no existe usuario con ese username/email");
                    return new UsernameNotFoundException("Usuario no encontrado");
                });
        return toPrincipal(user);
    }

    // Uso del filtro JWT: carga por id persistente (sub) y revalida roles/estado en cada petición (SEC-011)
    @Transactional(readOnly = true)
    public SecurityPrincipal loadPrincipalById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return toPrincipal(user);
    }

    // Búsqueda de identidad por username o email (un único punto de decisión)
    private Optional<User> findByUsernameOrEmail(String identifier) {
        Optional<User> byUsername = userRepository.findByUsername(identifier);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        return userRepository.findByEmailIgnoreCase(identifier);
    }

    private SecurityPrincipal toPrincipal(User user) {
        String passwordHash = userCredentialRepository.findByUserId(user.getId())
                .map(UserCredential::getPasswordHash)
                .orElse(null); // usuario legado sin credencial: no podrá autenticarse (deny-by-default)

        List<GrantedAuthority> authorities = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .filter(role -> role != null && !role.isBlank())
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase(Locale.ROOT)))
                .distinct()
                .toList();

        boolean active = Boolean.TRUE.equals(user.getActive());
        return new SecurityPrincipal(user.getId(), user.getUsername(), user.getEmail(), passwordHash, active, authorities);
    }
}
