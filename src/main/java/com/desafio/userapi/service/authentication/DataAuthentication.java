package com.desafio.userapi.service.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DataAuthentication(
        @NotBlank
        String email,
        @NotBlank
        String password,
        @NotNull
        TypeUser typeUser
) {
}
