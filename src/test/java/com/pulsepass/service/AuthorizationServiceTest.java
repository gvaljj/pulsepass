package com.pulsepass.service;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.security.SecurityErrorMessages;
import com.pulsepass.security.SecurityPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// PRD §4.5/§9.4: frontera de autorización con id persistente. Propiedad violada -> 403; borrador -> 404
// idéntico al inexistente (anti-enumeración).
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private static final Long ANDREA_ID = 42L;
    private static final Long LUIS_ID = 99L;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private SecurityPrincipal authenticate(Long userId, String... roles) {
        List<org.springframework.security.core.GrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .<org.springframework.security.core.GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        SecurityPrincipal principal = new SecurityPrincipal(userId, "user-" + userId,
                "user" + userId + "@email.com", "$2a$10$hash", true, authorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
        return principal;
    }

    private User user(Long id) {
        User user = new User("user-" + id, "user" + id + "@email.com");
        user.setId(id);
        return user;
    }

    private Ticket ticketOwnedBy(Long userId) {
        Ticket ticket = new Ticket();
        ticket.setTicketCode("TCK-" + userId);
        ticket.setUser(user(userId));
        return ticket;
    }

    private Event event(EventStatus status) {
        Event event = new Event();
        event.setEventCode("EV-2026");
        event.setStatus(status);
        return event;
    }

    // ---------------------------------------------------------------- identidad

    @Test
    @DisplayName("requirePrincipal sin autenticación -> AuthenticationCredentialsNotFoundException (401 defensivo)")
    void requirePrincipal_Anonimo_Lanza401() {
        assertThatThrownBy(() -> authorizationService.requirePrincipal())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessage(SecurityErrorMessages.UNAUTHENTICATED);
    }

    @Test
    @DisplayName("isPrivileged distingue STAFF/ADMIN de USER")
    void isPrivileged_SoloStaffYAdmin() {
        authenticate(ANDREA_ID, "USER");
        assertThat(authorizationService.isPrivileged()).isFalse();

        authenticate(ANDREA_ID, "STAFF");
        assertThat(authorizationService.isPrivileged()).isTrue();

        authenticate(ANDREA_ID, "ADMIN");
        assertThat(authorizationService.isPrivileged()).isTrue();
    }

    // ---------------------------------------------------------------- propiedad (anti-IDOR)

    @Test
    @DisplayName("ensureSelf: el mismo usuario pasa; otro usuario -> AccessDeniedException")
    void ensureSelf_MismoId_Pasa_OtroId_Deniega() {
        authenticate(ANDREA_ID, "USER");

        assertThatCode(() -> authorizationService.ensureSelf(ANDREA_ID)).doesNotThrowAnyException();
        assertThatThrownBy(() -> authorizationService.ensureSelf(LUIS_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(SecurityErrorMessages.ACCESS_DENIED);
    }

    @Test
    @DisplayName("ensureSelfOrAdmin: ADMIN accede a otros usuarios (USR-02/03)")
    void ensureSelfOrAdmin_AdminPasa() {
        authenticate(LUIS_ID, "ADMIN");

        assertThatCode(() -> authorizationService.ensureSelfOrAdmin(ANDREA_ID)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ensureSelfOrAdmin: STAFF queda EXCLUIDO de datos de usuarios (matriz de autorización)")
    void ensureSelfOrAdmin_StaffDeniega() {
        authenticate(LUIS_ID, "STAFF");

        assertThatThrownBy(() -> authorizationService.ensureSelfOrAdmin(ANDREA_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("ensureCanQueryUserTickets: email propio -> OK (TKT-03)")
    void ensureCanQueryUserTickets_EmailPropio_Pasa() {
        authenticate(ANDREA_ID, "USER");
        when(userRepository.findByEmailIgnoreCase("user42@email.com")).thenReturn(Optional.of(user(ANDREA_ID)));

        assertThatCode(() -> authorizationService.ensureCanQueryUserTickets("user42@email.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ensureCanQueryUserTickets: email ajeno -> 403 sin importar si existe o no (sin oráculo de usuarios)")
    void ensureCanQueryUserTickets_EmailAjeno_OInexistente_Deniega() {
        authenticate(ANDREA_ID, "USER");
        when(userRepository.findByEmailIgnoreCase("user99@email.com")).thenReturn(Optional.of(user(LUIS_ID)));
        when(userRepository.findByEmailIgnoreCase("nadie@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorizationService.ensureCanQueryUserTickets("user99@email.com"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> authorizationService.ensureCanQueryUserTickets("nadie@email.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("ensureCanQueryUserTickets: ADMIN consulta cualquier email")
    void ensureCanQueryUserTickets_AdminPasa() {
        authenticate(LUIS_ID, "ADMIN");

        assertThatCode(() -> authorizationService.ensureCanQueryUserTickets("user42@email.com"))
                .doesNotThrowAnyException();
    }

    // ---------------------------------------------------------------- tickets

    @Test
    @DisplayName("ensureTicketViewer: propietario y STAFF/ADMIN ven el ticket; otro usuario -> 403 (TKT-02)")
    void ensureTicketViewer_PropietarioYStaffPasan_AjenoDeniega() {
        Ticket ticket = ticketOwnedBy(ANDREA_ID);

        authenticate(ANDREA_ID, "USER");
        assertThatCode(() -> authorizationService.ensureTicketViewer(ticket)).doesNotThrowAnyException();

        authenticate(LUIS_ID, "STAFF");
        assertThatCode(() -> authorizationService.ensureTicketViewer(ticket)).doesNotThrowAnyException();

        authenticate(LUIS_ID, "USER");
        assertThatThrownBy(() -> authorizationService.ensureTicketViewer(ticket))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("ensureTicketOwnerOrAdmin: cancelar es del propietario o ADMIN; STAFF -> 403 (TKT-05)")
    void ensureTicketOwnerOrAdmin_StaffDeniega_AdminPasa() {
        Ticket ticket = ticketOwnedBy(ANDREA_ID);

        authenticate(LUIS_ID, "STAFF");
        assertThatThrownBy(() -> authorizationService.ensureTicketOwnerOrAdmin(ticket))
                .isInstanceOf(AccessDeniedException.class);

        authenticate(LUIS_ID, "ADMIN");
        assertThatCode(() -> authorizationService.ensureTicketOwnerOrAdmin(ticket)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ensurePrivileged: operaciones de staff deniegan a USER (TKT-04/06)")
    void ensurePrivileged_UserDeniega_StaffPasa() {
        authenticate(ANDREA_ID, "USER");
        assertThatThrownBy(authorizationService::ensurePrivileged)
                .isInstanceOf(AccessDeniedException.class);

        authenticate(ANDREA_ID, "STAFF");
        assertThatCode(authorizationService::ensurePrivileged).doesNotThrowAnyException();
    }

    // ---------------------------------------------------------------- eventos (EVT-02)

    @Test
    @DisplayName("ensureEventVisible: PUBLISHED es visible para cualquiera, incluso anónimo")
    void ensureEventVisible_Publicado_Pasa() {
        assertThatCode(() -> authorizationService.ensureEventVisible(event(EventStatus.PUBLISHED)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ensureEventVisible: borrador anónimo -> 404 con el MISMO mensaje que un inexistente (T-SEC-017)")
    void ensureEventVisible_BorradorAnonimo_404IdenticoAlInexistente() {
        Event draft = event(EventStatus.DRAFT);

        assertThatThrownBy(() -> authorizationService.ensureEventVisible(draft))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Evento no encontrado: EV-2026"); // idéntico al de un código inexistente
    }

    @Test
    @DisplayName("ensureEventVisible: STAFF/ADMIN sí ven borradores")
    void ensureEventVisible_BorradorStaff_Pasa() {
        Event draft = event(EventStatus.DRAFT);

        authenticate(ANDREA_ID, "STAFF");
        assertThatCode(() -> authorizationService.ensureEventVisible(draft)).doesNotThrowAnyException();

        authenticate(ANDREA_ID, "ADMIN");
        assertThatCode(() -> authorizationService.ensureEventVisible(draft)).doesNotThrowAnyException();
    }
}
