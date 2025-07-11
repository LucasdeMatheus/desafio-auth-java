package com.desafio.userapi.service.authentication;

import com.desafio.userapi.controller.TokenDTO;
import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import com.desafio.userapi.domain.user.User;
import com.desafio.userapi.domain.user.UserRepository;
import com.desafio.userapi.infra.TokenService;
import com.desafio.userapi.service.client.AuthorizationCodeService;
import com.desafio.userapi.service.client.GrantType;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthenticationService implements UserDetailsService {


    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationContext applicationContext;  // <-- inject context

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AuthorizationCodeService authorizationCodeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Client client = clientRepository.findByEmail(username);
        if (client != null) {
            return client;
        }
        User user = userRepository.findByEmail(username);
        if (user != null) {
            return user;
        }
        throw new UsernameNotFoundException("Usuário ou cliente não encontrado com email: " + username);
    }


    public ResponseEntity<?> login(@Valid DataAuthentication data, HttpSession session) {
        try {
            String token = "";
            String clientId = null;
            Client client = null;
            User user = null;
            Map<String, Object> response;

            if (session != null) {
                clientId = (String) session.getAttribute("client_id");
                System.out.println(clientId);
            }

            Optional<Client> clientSession = clientRepository.findByClientId(clientId);

            if (data.typeUser() == TypeUser.USER) {
                user = userRepository.findByEmail(data.email());
                if (user == null || user.isOauthUser()) {
                    return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas ou cadastro feito pelo Google"));
                }

                AuthenticationManager manager = applicationContext.getBean(AuthenticationManager.class);
                var authToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
                var authentication = manager.authenticate(authToken);

                token = clientSession.isPresent()
                        ? tokenService.gerarToken((User) authentication.getPrincipal(), clientSession.get().getScopes())
                        : tokenService.gerarToken(user, null);

            } else {
                client = clientRepository.findByEmail(data.email());
                if (client == null || client.isOauthUser()) {
                    return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas ou cadastro feito pelo Google"));
                }

                if (!passwordEncoder.matches(data.password(), client.getClientSecret())) {
                    return ResponseEntity.status(401).body(Map.of("error", "Senha inválida"));
                }

                token = tokenService.gerarToken(client);
            }

            if (clientSession.isPresent() && clientSession.get().getRedirectUri() != null) {
                session.removeAttribute("redirect_uri");
                session.removeAttribute("client_id");

                String code = authorizationCodeService.createCode(token);
                String redirectUri = clientSession.get().getRedirectUri();
                return ResponseEntity.status(HttpStatus.FOUND)
                        .body(redirectUri + "?code=" + code)
//                        .location(URI.create(redirectUri + "?code=" + code))
//                        .build();
                ;
            }

            response = data.typeUser().equals(TypeUser.USER)
                    ? Map.of("id", user.getId(), "email", user.getEmail(), "name", user.getName(), "token", token)
                    : Map.of("id", client.getId(), "email", client.getEmail(), "name", client.getClientId(), "token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }
    }






}
