package com.desafio.user_api.service.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateEmailDTO (
        @NotBlank
        String email
){
}
