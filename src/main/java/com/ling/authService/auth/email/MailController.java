package com.ling.authService.auth.email;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class MailController {

    private final MailService service;

    public MailController(MailService service) {
        this.service = service;
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Void> verify(@RequestParam String code) {
        try {
            service.verify(code);
            return ResponseEntity.ok().build();
        } catch (EmailAlreadyVerifiedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
