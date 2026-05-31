package com.ling.authService.auth.dto.register;

public record RegisterRequest(
        String username,
        String email,
        String password
) {}
