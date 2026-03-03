package com.moneycontas.dto.response;

public record AuthResponse(
        String token,
        long expiresIn,
        String name,
        String email) {
}
