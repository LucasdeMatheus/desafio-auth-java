package com.desafio.userapi.service.client;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthorizationCodeService {

    // Mapa código -> token JWT
    private final Map<String, String> codeTokenMap = new ConcurrentHashMap<>();

    // Gera código, associa ao token e guarda no mapa
    public String createCode(String token) {
        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        codeTokenMap.put(code, token);
        return code;
    }

    // Recupera token pelo código
    public String getTokenByCode(String code) {
        return codeTokenMap.get(code);
    }

    // Remove código após uso
    public void removeCode(String code) {
        codeTokenMap.remove(code);
    }
}
