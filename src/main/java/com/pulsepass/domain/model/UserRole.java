package com.pulsepass.domain.model;

import jakarta.persistence.*;

// PRD Capa de Seguridad §9.1: rol del usuario (USER / STAFF / ADMIN).
// PK compuesta (user_id, role): un usuario puede tener varios roles, nunca repetidos.
@Entity
@Table(name = "user_roles")
@IdClass(UserRoleKey.class)
public class UserRole {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "role", nullable = false, length = 30)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public UserRole() {}

    public UserRole(User user, String role) {
        this.userId = user.getId();
        this.user = user;
        this.role = role;
    }

    // Getters y Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }
}
