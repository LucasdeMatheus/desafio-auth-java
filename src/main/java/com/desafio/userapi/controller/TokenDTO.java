package com.desafio.userapi.controller;

import com.desafio.userapi.service.client.GrantType;
import jakarta.validation.constraints.NotBlank;

public record TokenDTO (
        @NotBlank GrantType grantType,
        @NotBlank String client_id,
        @NotBlank String code,
        @NotBlank String client_secret
){
}
