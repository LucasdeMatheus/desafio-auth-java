package com.desafio.userapi.service.user;

import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
public class CodeService {
    public String getCode() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }
    private final Map<String, String> codeMap = new HashMap<>();

    public void storeCode(@NotBlank String email, String code) {
        codeMap.put(email, code);
    }

    public boolean isCodeValid(String email, String code) {
        System.out.println(code + " " + codeMap.get(email));
        return code.equals(codeMap.get(email));
    }

    public void removeCode(String email) {
        codeMap.remove(email);
    }

    public Map<String, String> getCodeMap() {
        return codeMap;
    }
}
