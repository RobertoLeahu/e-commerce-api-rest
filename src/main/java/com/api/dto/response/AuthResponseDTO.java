package com.api.dto.response;

public record AuthResponseDTO(
        String token,
        String type,
        String email,
        String rol
) {
    public AuthResponseDTO(String token, String email, String rol) {
        this(token, "Bearer", email, rol);
    }
}
