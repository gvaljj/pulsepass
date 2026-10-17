package com.pulsepass.domain.model;

import java.io.Serializable;
import java.util.Objects;

// Clave compuesta de UserRole (PK user_id + role) exigida por JPA con @IdClass.
public class UserRoleKey implements Serializable {

    private Long userId;
    private String role;

    public UserRoleKey() {}

    public UserRoleKey(Long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleKey that = (UserRoleKey) o;
        return Objects.equals(userId, that.userId) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, role);
    }
}
