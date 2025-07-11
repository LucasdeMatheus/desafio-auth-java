package com.desafio.userapi.service.client;

import com.desafio.userapi.controller.TokenDTO;
import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import com.desafio.userapi.domain.user.User;
import com.desafio.userapi.domain.user.UserRepository;
import com.desafio.userapi.infra.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ClientService{

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AuthorizationCodeService authorizationCodeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;

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

    public ResponseEntity<?> handleGrantType(TokenDTO data) {
        GrantType grantType;

        try {
            grantType = data.grantType();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("grant_type inválido");
        }

        return switch (grantType) {
            case AUTHORIZATION_CODE -> handleAuthorizationCode(data);
            case REFRESH_TOKEN      -> handleRefreshToken(data);
            case CLIENT_CREDENTIALS -> handleClientCredentials(data);
        };
    }

    public ResponseEntity<?> handleClientCredentials(TokenDTO data) {
        Optional<Client> clientOpt = clientRepository.findByClientId(data.client_id());

        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Client ID inválido"));
        }

        Client client = clientOpt.get();

        if (!passwordEncoder.matches(data.client_secret(), client.getClientSecret())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Client Secret inválido"));
        }

        String token = tokenService.gerarToken(client);

        return ResponseEntity.ok(Map.of(
                "access_token", token,
                "expires_in", 3600
        ));
    }




    public ResponseEntity<?> handleAuthorizationCode(TokenDTO data) {
        Optional<Client> clientOpt = clientRepository.findByClientId(data.client_id());

        if (clientOpt.isEmpty() ||
                !passwordEncoder.matches(data.client_secret(), clientOpt.get().getClientSecret())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Client inválido"));
        }

        String token = authorizationCodeService.getTokenByCode(data.code());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Código inválido ou expirado"));
        }

        // Código só pode ser usado uma vez
        authorizationCodeService.removeCode(data.code());

        String code = authorizationCodeService.createCode(token);
        // Retorna token JWT
        return ResponseEntity.ok(Map.of(
                "access_token", token,
                "refresh_token", code
        ));
    }

    public ResponseEntity<?> handleRefreshToken(TokenDTO data) {
        Optional<Client> clientOpt = clientRepository.findByClientId(data.client_id());

        if (clientOpt.isEmpty() ||
                !passwordEncoder.matches(data.client_secret(), clientOpt.get().getClientSecret())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Client inválido"));
        }
        String oldToken = authorizationCodeService.getTokenByCode(data.code());
        if (oldToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Código inválido ou expirado"));
        }

        // Código só pode ser usado uma vez
        authorizationCodeService.removeCode(data.code());

        String subject = tokenService.getSubject(oldToken);
        String type = tokenService.getType(oldToken);
        Set<Scope> scopes = clientOpt.get().getScopes();

        String newAccessToken;
        User user = userRepository.findById(Long.parseLong(subject))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        newAccessToken = tokenService.gerarToken(user, scopes);

        String newCode =  authorizationCodeService.createCode(newAccessToken);
        return ResponseEntity.ok(Map.of(
                "access_token", newAccessToken,
                "refresh_token", newCode
        ));
    }
}
