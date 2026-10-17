package com.pulsepass.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// PRD §9.2: identidad autenticada construida desde la base de datos (id persistente + roles vigentes),
// no desde el contenido del JWT. El JWT solo aporta sub (id); roles y estado se revalidan en cada petición.
public class SecurityPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String username;
    private final String email;
    private final String password;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public SecurityPrincipal(Long userId, String username, String email, String password,
                             boolean active, List<GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.active = active;
        this.authorities = List.copyOf(authorities);
    }

    // Id persistente: base de las comprobaciones anti-IDOR (nunca comparar solo strings de email)
    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Usuario inactivo: no supera la autenticación ni conserva acceso en peticiones siguientes (SEC-011)
    @Override
    public boolean isEnabled() {
        return active;
    }

    public boolean isActive() {
        return active;
    }
}
