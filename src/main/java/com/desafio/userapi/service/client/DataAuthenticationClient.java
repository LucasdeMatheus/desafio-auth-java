package com.desafio.userapi.service.client;

import jakarta.validation.constraints.NotBlank;

public record DataAuthenticationClient(
        @NotBlank
        String email,
        @NotBlank
        String clientSecret) {
}
