package com.desafio.user_api.service.register;

import jakarta.validation.constraints.NotBlank;

public record DataRegister(
        @NotBlank
        String name,
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
