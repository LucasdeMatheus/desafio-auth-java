package com.desafio.userapi.service.client;

import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClientService{

    @Autowired
    private ClientRepository clientRepository;


    public ResponseEntity<?> config(ConfigDTO data, Client client) {
        try {
            Map<String, Object> response = new HashMap<>();


            if (data.scopes() != null) {
                client.setScopes(data.scopes());
                response.put("scopes", data.scopes());
            }
            if (data.grantTypes() != null) {
                client.setGrantTypes(data.grantTypes());
                response.put("grantTypes", data.grantTypes());
            }
            if (data.redirectUri() != null && !data.redirectUri().isBlank()) {
                client.setRedirectUri(data.redirectUri());
                response.put("redirectUri", data.redirectUri());
            }

            clientRepository.save(client);

            return ResponseEntity.ok(response);
        }catch (Exception e) {
            e.printStackTrace();  // para ver detalhes no log
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao configurar"));
        }
    }
}
