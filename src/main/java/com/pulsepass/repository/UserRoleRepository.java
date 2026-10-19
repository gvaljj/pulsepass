package com.pulsepass.repository;

import com.pulsepass.domain.model.UserRole;
import com.pulsepass.domain.model.UserRoleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleKey> {

    // CustomUserDetailsService / AuthorizationService: roles vigentes del usuario (una sola query, sin N+1)
    List<UserRole> findByUserId(Long userId);
}
