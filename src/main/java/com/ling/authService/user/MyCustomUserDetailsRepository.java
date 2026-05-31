package com.ling.authService.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MyCustomUserDetailsRepository extends JpaRepository<MyCustomUserDetails, UUID> {
    Optional<MyCustomUserDetails> findByEmail(String email);
    Optional<MyCustomUserDetails> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}

