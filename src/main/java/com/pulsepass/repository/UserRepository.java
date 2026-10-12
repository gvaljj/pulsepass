package com.pulsepass.repository;

import com.pulsepass.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // PRD §32: buscar usuario por username
    Optional<User> findByUsername(String username);

    // PRD §32: buscar usuario por email ignorando mayúsculas
    Optional<User> findByEmailIgnoreCase(String email);

    // BR-USER-001 / PRD §32: validar unicidad de username
    boolean existsByUsername(String username);

    // BR-USER-002 / PRD §32: validar unicidad de email ignorando mayúsculas
    boolean existsByEmailIgnoreCase(String email);
}