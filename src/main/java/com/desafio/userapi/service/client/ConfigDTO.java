package com.desafio.userapi.service.client;

import jakarta.persistence.ElementCollection;

import java.util.Set;

public record ConfigDTO(
        String redirectUri,
        Set<Scope>scopes,
        Set<GrantType> grantTypes
) {
}
