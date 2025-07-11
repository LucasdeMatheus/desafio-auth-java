package com.desafio.userapi.infra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.service.client.Scope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.desafio.userapi.domain.user.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private static final String ISSUER = "user-api";

    public String gerarToken(User user, Set<Scope> scopes) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            JWTCreator.Builder builder = JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(String.valueOf(user.getId()))
                    .withClaim("type", "user")
                    .withExpiresAt(dataExpiracao());

            // Escopos dinâmicos
            if (scopes != null) {

                if (scopes.contains(Scope.EMAIL)) {
                    builder.withClaim("email", user.getEmail());
                }
                if (scopes.contains(Scope.NAME)) {
                    builder.withClaim("name", user.getName());
                }

                // Também pode incluir o escopo no token para registrar o que foi concedido
                List<String> scopeList = scopes.stream().map(Enum::name).toList();
                builder.withClaim("scopes", scopeList);
            }

            return builder.sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String gerarToken(Client client) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(String.valueOf(client.getId()))
                    .withClaim("clientId", client.getClientId())
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!", exception);
        }
    }

    public String getType(String tokenJWT) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(tokenJWT)
                    .getClaim("type").asString();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!", exception);
        }
    }
    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }


}
