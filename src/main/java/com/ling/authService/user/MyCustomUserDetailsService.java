package com.ling.authService.user;

import com.ling.authService.common.EntityAlreadyExistsException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MyCustomUserDetailsService {

    private final MyCustomUserDetailsRepository repository;

    public MyCustomUserDetailsService(MyCustomUserDetailsRepository repository) {
        this.repository = repository;
    }

    public MyCustomUserDetails save(MyCustomUserDetails myCustomUserDetails) {
        return repository.save(myCustomUserDetails);
    }

    public MyCustomUserDetails create(MyCustomUserDetails myCustomUserDetails) {
        if (repository.existsByEmail(myCustomUserDetails.getEmail())) {
            throw new EntityAlreadyExistsException("Пользователь с таким email уже существует");
        }
        if (repository.existsByUsername(myCustomUserDetails.getUsername())) {
            throw new EntityAlreadyExistsException("Пользователь с таким username уже существует");
        }
        return save(myCustomUserDetails);
    }

    public MyCustomUserDetails getUserDetailsByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }

    public MyCustomUserDetails getUserDetailsByUuid(UUID uuid) {
        return repository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + uuid));
    }

    public MyCustomUserDetails getUserDetailsByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }
}
