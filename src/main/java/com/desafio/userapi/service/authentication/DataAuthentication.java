package com.desafio.userapi.service.authentication;

import jakarta.validation.constraints.NotBlank;

public record DataAuthentication(
        @NotBlank
        String email,
        @NotBlank
        String password) {
}
