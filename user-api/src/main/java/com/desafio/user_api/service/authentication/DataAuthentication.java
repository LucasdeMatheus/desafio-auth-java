package com.desafio.user_api.service.authentication;

import jakarta.validation.constraints.NotBlank;

public record DataAuthentication(
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
