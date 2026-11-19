package com.api.dto.response;

import com.api.domain.enums.Rol;

public record AuthResponseDTO(
        String authorizationToken,
        String type,
        String email,
        Rol rol
) {
    public AuthResponseDTO(String token, String email, Rol rol) {
        this(token, "Bearer", email, rol);
    }
}