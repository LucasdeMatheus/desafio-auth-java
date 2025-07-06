package com.desafio.userapi.service.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateEmailDTO (
        @NotBlank
        String email
){
}
