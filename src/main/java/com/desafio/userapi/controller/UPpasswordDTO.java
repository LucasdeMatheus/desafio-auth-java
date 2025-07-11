package com.desafio.userapi.controller;

import com.desafio.userapi.service.authentication.TypeUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UPpasswordDTO(
        @NotBlank
        String email,
        @NotNull
        TypeUser typeUser
) {
}
