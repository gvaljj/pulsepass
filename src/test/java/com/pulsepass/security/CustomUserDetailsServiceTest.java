package com.pulsepass.security;

import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserCredential;
import com.pulsepass.domain.model.UserRole;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// PRD §9.2: la identidad se resuelve desde la base de datos por username O email, con roles vigentes.
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCredentialRepository userCredentialRepository;
    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User user(Long id, boolean active) {
        User user = new User("andrea", "andrea@email.com");
        user.setId(id);
        user.setActive(active);
        return user;
    }

    @Test
    @DisplayName("Resuelve por username: principal con id, email, hash de contraseña, estado y roles ROLE_*")
    void loadUserByUsername_Existente_RetornaPrincipalCompleto() {
        User user = user(5L, true);
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(user));
        when(userCredentialRepository.findByUserId(5L))
                .thenReturn(Optional.of(new UserCredential(user, "$2a$10$hashsecreto")));
        when(userRoleRepository.findByUserId(5L)).thenReturn(List.of(
                new UserRole(user, "USER"),
                new UserRole(user, "STAFF")));

        SecurityPrincipal principal = (SecurityPrincipal) userDetailsService.loadUserByUsername("andrea");

        assertThat(principal.getUserId()).isEqualTo(5L);
        assertThat(principal.getUsername()).isEqualTo("andrea");
        assertThat(principal.getEmail()).isEqualTo("andrea@email.com");
        assertThat(principal.getPassword()).isEqualTo("$2a$10$hashsecreto");
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_STAFF");
    }

    @Test
    @DisplayName("Resuelve por email cuando el username no coincide (FR-AUTH-002)")
    void loadUserByUsername_PorEmail_RetornaPrincipal() {
        User user = user(5L, true);
        when(userRepository.findByUsername("andrea@email.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(userCredentialRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(userRoleRepository.findByUserId(5L)).thenReturn(List.of(new UserRole(user, "USER")));

        SecurityPrincipal principal = (SecurityPrincipal) userDetailsService.loadUserByUsername("andrea@email.com");

        assertThat(principal.getUserId()).isEqualTo(5L);
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Usuario inexistente -> UsernameNotFoundException (nunca distingue tipo de identificador)")
    void loadUserByUsername_NoExiste_LanzaUsernameNotFound() {
        when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("fantasma"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Usuario legado sin credencial (V5): el principal se construye con contraseña nula -> deny-by-default")
    void loadUserByUsername_SinCredencial_PasswordNula() {
        User user = user(5L, true);
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(user));
        when(userCredentialRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(userRoleRepository.findByUserId(5L)).thenReturn(List.of());

        SecurityPrincipal principal = (SecurityPrincipal) userDetailsService.loadUserByUsername("andrea");

        assertThat(principal.getPassword()).isNull();
    }

    @Test
    @DisplayName("Roles vacíos o duplicados se filtran (solo roles únicos y no vacíos)")
    void loadUserByUsername_RolesDuplicados_OVacios_SeFiltran() {
        User user = user(5L, true);
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(user));
        when(userCredentialRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(userRoleRepository.findByUserId(5L)).thenReturn(List.of(
                new UserRole(user, "USER"),
                new UserRole(user, "USER"),
                new UserRole(user, "")));

        SecurityPrincipal principal = (SecurityPrincipal) userDetailsService.loadUserByUsername("andrea");

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("loadPrincipalById devuelve la identidad vigente por id persistente (sub del JWT)")
    void loadPrincipalById_Existente_RetornaPrincipal() {
        User user = user(9L, true);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(userCredentialRepository.findByUserId(9L)).thenReturn(Optional.empty());
        when(userRoleRepository.findByUserId(9L)).thenReturn(List.of(new UserRole(user, "ADMIN")));

        SecurityPrincipal principal = userDetailsService.loadPrincipalById(9L);

        assertThat(principal.getUserId()).isEqualTo(9L);
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadPrincipalById de un usuario desactivado devuelve principal con enabled=false (SEC-011)")
    void loadPrincipalById_Desactivado_EnabledFalse() {
        User user = user(9L, false);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(userCredentialRepository.findByUserId(9L)).thenReturn(Optional.empty());
        when(userRoleRepository.findByUserId(9L)).thenReturn(List.of(new UserRole(user, "USER")));

        assertThat(userDetailsService.loadPrincipalById(9L).isEnabled()).isFalse();
    }

    @Test
    @DisplayName("loadPrincipalById de un id inexistente -> UsernameNotFoundException (el token se descarta)")
    void loadPrincipalById_NoExiste_LanzaUsernameNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadPrincipalById(404L))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
