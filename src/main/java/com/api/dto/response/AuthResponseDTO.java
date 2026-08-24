package com.api.dto.response;

import com.api.domain.enums.Rol;

public record AuthResponseDTO(
        String token,
        String type,
        String email,
        Rol rol
) {
    public AuthResponseDTO(String token, String email, Rol rol) {
        this(token, "Bearer", email, rol);
    }
}
