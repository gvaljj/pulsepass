package com.pulsepass.service.impl;

import com.pulsepass.domain.model.User;
import com.pulsepass.domain.model.UserProfile;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.AuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    // Fase 4: la frontera de autorización se inyecta en el Service (mock = no-op en estas pruebas de reglas)
    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterUserRequest request() {
        return new RegisterUserRequest(
                "andrea", "andrea@email.com", "Andrea", "Gomez",
                "300", "Santa Marta", LocalDate.now().minusYears(25), "Segura#2026x");
    }

    // TEST-USER-001: registrar usuario válido crea User y UserProfile
    @Test
    @DisplayName("TEST-USER-001: registrar usuario válido crea User con perfil (BR-USER-004)")
    void register_ValidUser_CreatesUserAndProfile() {
        RegisterUserRequest request = request();
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(
                new UserResponse(1L, "andrea", "andrea@email.com", "Andrea", "Gomez",
                        "300", "Santa Marta", LocalDate.now().minusYears(25), true));

        UserResponse result = userService.register(request);

        assertThat(result.username()).isEqualTo("andrea");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getProfile()).isNotNull();
        assertThat(saved.getProfile().getFirstName()).isEqualTo("Andrea");
        assertThat(saved.getActive()).isTrue(); // BR-USER-003
    }

    // TEST-USER-002: username duplicado lanza DuplicateResourceException
    @Test
    @DisplayName("TEST-USER-002: username duplicado lanza DuplicateResourceException")
    void register_DuplicateUsername_Throws() {
        when(userRepository.existsByUsername("andrea")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    // TEST-USER-003: email duplicado lanza DuplicateResourceException
    @Test
    @DisplayName("TEST-USER-003: email duplicado lanza DuplicateResourceException")
    void register_DuplicateEmail_Throws() {
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    // TEST-USER-004: fecha de nacimiento futura lanza BusinessRuleException
    @Test
    @DisplayName("TEST-USER-004: birthDate futura lanza BusinessRuleException")
    void register_FutureBirthDate_Throws() {
        RegisterUserRequest future = new RegisterUserRequest(
                "andrea", "andrea@email.com", "Andrea", "Gomez",
                "300", "Santa Marta", LocalDate.now().plusDays(1), "Segura#2026x");
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.register(future))
                .isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any(User.class));
    }
}
