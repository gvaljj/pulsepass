package com.pulsepass.service;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.model.Event;
import com.pulsepass.domain.model.Ticket;
import com.pulsepass.domain.model.User;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.security.SecurityErrorMessages;
import com.pulsepass.security.SecurityPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// PRD §4.5 y §9.4: frontera de autorización central. Los controles de propiedad NO viven solo en los
// Controllers: los Services la invocan antes de leer o mutar recursos (anti-IDOR con el id persistente,
// comparando identidades, nunca solo strings de email — §9.4).
// Política consistente (decisión D2): propiedad violada sobre un recurso existente => 403;
// recurso que no debe revelarse al solicitante (borrador) => 404 idéntico al de un evento inexistente.
@Service
public class AuthorizationService {

    private static final String ROLE_STAFF = "ROLE_STAFF";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ---------------------------------------------------------------- identidad actual

    /** true si hay una identidad de la aplicación autenticada en el contexto de seguridad. */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getPrincipal() instanceof SecurityPrincipal;
    }

    /** STAFF o ADMIN (identidades privilegiadas para acciones indicadas, no propietarios automáticos). */
    public boolean isPrivileged() {
        return hasAuthority(ROLE_STAFF) || hasAuthority(ROLE_ADMIN);
    }

    /** Solo ADMIN. */
    public boolean isAdmin() {
        return hasAuthority(ROLE_ADMIN);
    }

    /** Principal actual. Sin autenticación => 401; identidad sin principal propio => 403 (deny-by-default). */
    public SecurityPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException(SecurityErrorMessages.UNAUTHENTICATED);
        }
        if (!(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            throw new AccessDeniedException(SecurityErrorMessages.ACCESS_DENIED);
        }
        return principal;
    }

    // ---------------------------------------------------------------- controles de propiedad

    /** Compra y autoservicio estricto: el recurso debe pertenecer al principal (por id persistente). */
    public void ensureSelf(Long userId) {
        SecurityPrincipal principal = requirePrincipal();
        if (!principal.getUserId().equals(userId)) {
            throw new AccessDeniedException(SecurityErrorMessages.ACCESS_DENIED);
        }
    }

    /** USR-02/USR-03: propio usuario o ADMIN (STAFF queda excluido por la matriz de autorización). */
    public void ensureSelfOrAdmin(Long userId) {
        if (isAdmin()) {
            return;
        }
        ensureSelf(userId);
    }

    /**
     * TKT-03: consultar tickets por email solo del propio usuario o ADMIN.
     * Si el email no existe o pertenece a otro => 403 (sin oráculo de existencia de usuarios).
     */
    public void ensureCanQueryUserTickets(String email) {
        if (isAdmin()) {
            return;
        }
        SecurityPrincipal principal = requirePrincipal();
        User target = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (target == null || !principal.getUserId().equals(target.getId())) {
            throw new AccessDeniedException(SecurityErrorMessages.ACCESS_DENIED);
        }
    }

    /** TKT-02: ticket propio, o STAFF/ADMIN en operación autorizada. */
    public void ensureTicketViewer(Ticket ticket) {
        if (isPrivileged()) {
            return;
        }
        ensureTicketOwner(ticket);
    }

    /** TKT-05: cancelación por el propietario (USER) o ADMIN; STAFF excluido. */
    public void ensureTicketOwnerOrAdmin(Ticket ticket) {
        if (isAdmin()) {
            return;
        }
        ensureTicketOwner(ticket);
    }

    /** TKT-04/TKT-06: operaciones de staff sobre tickets. */
    public void ensurePrivileged() {
        if (!isPrivileged()) {
            throw new AccessDeniedException(SecurityErrorMessages.ACCESS_DENIED);
        }
    }

    /**
     * EVT-02: un evento PUBLISHED es visible para cualquiera; el resto de estados solo para STAFF/ADMIN.
     * Si no es visible responde EXACTAMENTE igual que un evento inexistente (404 con el mismo cuerpo),
     * de modo que un borrador nunca se revela ni se enumera (T-SEC-017).
     */
    public void ensureEventVisible(Event event) {
        if (event.getStatus() == EventStatus.PUBLISHED || isPrivileged()) {
            return;
        }
        throw new ResourceNotFoundException("Evento no encontrado: " + event.getEventCode());
    }

    // ---------------------------------------------------------------- internos

    private boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    private void ensureTicketOwner(Ticket ticket) {
        SecurityPrincipal principal = requirePrincipal();
        if (ticket.getUser() == null || !principal.getUserId().equals(ticket.getUser().getId())) {
            throw new AccessDeniedException(SecurityErrorMessages.ACCESS_DENIED);
        }
    }
}
