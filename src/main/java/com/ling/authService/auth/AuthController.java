package com.ling.authService.auth;

import com.ling.authService.auth.dto.login.LoginRequest;
import com.ling.authService.auth.dto.login.LoginResponse;
import com.ling.authService.auth.dto.refresh.RefreshRequest;
import com.ling.authService.auth.dto.refresh.RefreshResponse;
import com.ling.authService.auth.dto.register.RegisterRequest;
import com.ling.authService.security.jwt.exception.InvalidTokenException;
import com.ling.authService.security.jwt.exception.InvalidTokenTypeException;
import com.ling.authService.security.jwt.JwtService;
import com.ling.authService.security.jwt.TokenType;
import com.ling.authService.user.MyCustomUserDetails;
import com.ling.authService.user.MyCustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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
        authService.register(request.username(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        if (!jwtService.isRefreshToken(request.refreshToken())) {
            throw new InvalidTokenTypeException("Invalid token type: " + jwtService.extractTokenType(request.refreshToken()));
        }

        String username = jwtService.extractUserName(request.refreshToken());
        MyCustomUserDetails user = myCustomUserDetailsService.getUserDetailsByUsername(username);

        if (!jwtService.isTokenValid(request.refreshToken(), user)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        return ResponseEntity.ok(
                new RefreshResponse(
                        jwtService.generateToken(user, Duration.ofMinutes(15), TokenType.ACCESS)
                )
        );
    }


}
