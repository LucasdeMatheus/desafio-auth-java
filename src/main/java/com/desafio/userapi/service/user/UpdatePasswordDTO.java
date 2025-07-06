package com.desafio.userapi.service.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordDTO (
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,11}$",
                message = "A senha deve ter entre 8 e 11 caracteres, incluir pelo menos uma letra maiúscula, uma minúscula e um caractere especial (@#$%^&+=)")
        String password
){
}
