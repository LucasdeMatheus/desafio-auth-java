package com.desafio.user_api.service.user;

import com.desafio.user_api.domain.User;
import com.desafio.user_api.domain.UserRepository;
import com.desafio.user_api.infra.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;


    @Autowired
    private AuthenticationManager manager;
    public ResponseEntity<Map<String, Object>> register(UserDTO data) {
        try {
            if (userRepository.existsByEmail(data.email())){
                return ResponseEntity.status(401).body(Map.of("error", "Email já está em uso"));
            }
            // validar e-mail com envio de código paraa gmail //
            ////////////////////////////////////////////////////
            User user = new User(data);
            user.setPassword(passwordEncoder.encode(data.password()));

            userRepository.save(user);

            // Authenticate and generate token JWT
            var authenticationToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            var authentication = manager.authenticate(authenticationToken);
            var tokenJWT = tokenService.gerarToken((User) authentication.getPrincipal());

            // success message
            Map<String, Object> userInfo = Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName() // Adicione mais atributos se necessário
            );

            Map<String, Object> response = Map.of(
                    "message", "Login realizado com sucesso",
                    "token", tokenJWT,
                    "user", userInfo
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Email já está em uso"));
            }
    }
}
