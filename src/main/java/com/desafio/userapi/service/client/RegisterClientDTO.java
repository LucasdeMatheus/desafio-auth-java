package com.desafio.userapi.service.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record RegisterClientDTO(
        @NotBlank
        String clientId,
        @NotBlank
        String clientSecret,

        @NotBlank
        String code
) {}
