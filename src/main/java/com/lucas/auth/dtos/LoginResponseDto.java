package com.lucas.auth.dtos;

public record LoginResponseDto (String accessToken, String refreshToken, Long expiresIn) {
}
