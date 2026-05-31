package com.ling.authService.auth;

import com.ling.authService.auth.dto.login.LoginRequest;
import com.ling.authService.auth.dto.login.LoginResponse;
import com.ling.authService.auth.dto.refresh.RefreshRequest;
import com.ling.authService.auth.dto.refresh.RefreshResponse;
import com.ling.authService.auth.dto.register.RegisterRequest;
import com.ling.authService.auth.email.EmailNotVerifiedException;
import com.ling.authService.security.jwt.JwtService;
import com.ling.authService.security.jwt.TokenType;
import com.ling.authService.user.MyCustomUserDetails;
import com.ling.authService.user.MyCustomUserDetailsRepository;
import com.ling.authService.user.MyCustomUserDetailsService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;
    private final MyCustomUserDetailsService myCustomUserDetailsService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, MyCustomUserDetailsService myCustomUserDetailsService, JwtService jwtService) {
        this.authService = authService;
        this.myCustomUserDetailsService = myCustomUserDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request.username(), request.email(), request.password());
            return ResponseEntity.ok().build();
        } catch (EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request.email(), request.password()));
        } catch (BadCredentialsException | EntityNotFoundException | EmailNotVerifiedException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        if (!jwtService.isRefreshToken(request.refreshToken())) {
            return ResponseEntity.badRequest().build();
        }

        String username = jwtService.extractUserName(request.refreshToken());
        MyCustomUserDetails user = myCustomUserDetailsService.getUserDetailsByUsername(username);

        if (!jwtService.isTokenValid(request.refreshToken(), user)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                new RefreshResponse(
                        jwtService.generateToken(user, Duration.ofMinutes(15), TokenType.ACCESS)
                )
        );
    }


}
