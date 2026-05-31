package com.ling.authService.auth.dto.login;

public record LoginResponse(String accessToken, String refreshToken) {
}
