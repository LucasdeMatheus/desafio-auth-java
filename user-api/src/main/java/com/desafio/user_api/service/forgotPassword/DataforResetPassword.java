package com.desafio.user_api.service.forgotPassword;

import jakarta.validation.constraints.NotBlank;

public record DataforResetPassword(
        @NotBlank
        String token,
        @NotBlank
        String newPassword
) {
}
