package com.desafio.user_api.service.forgotPassword;

import jakarta.validation.constraints.NotBlank;

public record DataEmail(
        @NotBlank
        String email
) {
}
