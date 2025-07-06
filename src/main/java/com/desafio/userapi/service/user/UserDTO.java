package com.desafio.userapi.service.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserDTO (
        @NotBlank
        String name,
        @NotBlank
        String email
){
}
