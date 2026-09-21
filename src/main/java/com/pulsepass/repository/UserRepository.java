package com.pulsepass.repository;

import com.pulsepass.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // FR-USR-002: Buscar usuario por username
    Optional<User> findByUsername(String username);

    // FR-USR-002: Buscar usuario por email ignorando mayúsculas
    Optional<User> findByEmailIgnoreCase(String email);
}