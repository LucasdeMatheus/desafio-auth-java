package com.desafio.user_api.service.authentication;

import com.desafio.user_api.domain.User;
import com.desafio.user_api.domain.UserRepository;
import com.desafio.user_api.service.user.UserService;
import com.desafio.user_api.infra.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;  // <-- inject context

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmail(username);
    }

    public ResponseEntity<Map<String, Object>> login(@Valid DataAuthentication data) {
        try {
            String email = data.email();

            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
            }

            // Busca o AuthenticationManager **na hora que precisar**, quebrando o ciclo
            AuthenticationManager manager = applicationContext.getBean(AuthenticationManager.class);

            var authenticationToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            var authentication = manager.authenticate(authenticationToken);
            var tokenJWT = tokenService.gerarToken((User) authentication.getPrincipal());

            Map<String, Object> userInfo = Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName()
            );

            Map<String, Object> response = Map.of(
                    "message", "Login realizado com sucesso",
                    "token", tokenJWT,
                    "user", userInfo
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }
    }
}
