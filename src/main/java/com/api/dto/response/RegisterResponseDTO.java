package com.api.dto.response;

public record RegisterResponseDTO(
        String mensaje,
        String activationToken
) {}