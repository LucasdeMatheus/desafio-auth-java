package com.desafio.userapi.service.authentication;

import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import com.desafio.userapi.domain.user.User;
import com.desafio.userapi.domain.user.UserRepository;
import com.desafio.userapi.infra.TokenService;
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
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

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
            AuthenticationManager manager = applicationContext.getBean(AuthenticationManager.class);
            var authenticationToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            var authentication = manager.authenticate(authenticationToken);

            String token;
            String redirectUri = (String) session.getAttribute("redirect_uri");

            if (data.typeUser() == TypeUser.USER) {
                User user = userRepository.findByEmail(data.email());
                if (user == null || user.isOauthUser()) {
                    return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas ou cadastro feito pelo Google"));
                }
                token = tokenService.gerarToken(user);
            } else {
                Client client = clientRepository.findByEmail(data.email());
                if (client == null || client.isOauthUser()) {
                    return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas ou cadastro feito pelo Google"));
                }
                token = tokenService.gerarToken(client);
            }

            if (redirectUri != null) {
                // Remove dados da sessão
                session.removeAttribute("redirect_uri");
                session.removeAttribute("client_id");

                // Monta a URI de redirecionamento com token na query string
                String redirectWithToken = redirectUri + "?token=" + token;

                return ResponseEntity.ok(redirectWithToken);
            } else {
                // Retorna JSON normal se não for fluxo SSO
                return ResponseEntity.ok(Map.of(
                        "message", "Login realizado com sucesso",
                        "token", token
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }
    }


}
