package com.pulsepass.repository;

import com.pulsepass.domain.model.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    // CustomUserDetailsService: cargar la credencial del usuario para validar la contraseña
    Optional<UserCredential> findByUserId(Long userId);

    // AuthService: validar unicidad lógica antes de persistir una credencial
    boolean existsByUserId(Long userId);
}
