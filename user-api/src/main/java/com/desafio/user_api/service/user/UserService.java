package com.desafio.user_api.service.user;

import com.desafio.user_api.domain.User;
import com.desafio.user_api.domain.UserRepository;
import com.desafio.user_api.infra.TokenService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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
                    "name", user.getName()
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

    public ResponseEntity<?> updateEmail(@NotBlank String email, Long id) {
        try {
            if (userRepository.existsByEmail(email)){
                return ResponseEntity.status(401).body(Map.of("error", "Email já está em uso"));
            }
            if (userRepository.findById(id).isEmpty()){
                return ResponseEntity.status(401).body(Map.of("error", "user não encontrado"));
            }
            User user = userRepository.findById(id).get();
            // verifica email //
            ////////////////////
            user.setEmail(email);
            userRepository.save(user);

            // success message
            Map<String, Object> response = Map.of(
                    "message", "Email trocado com sucesso"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Email já está em uso, ou não autorizado"));
        }
    }

    public ResponseEntity<?> updatePassword(String password, Long id) {
        try {
            if (userRepository.findById(id).isEmpty()){
                return ResponseEntity.status(401).body(Map.of("error", "user não encontrado"));
            }
            User user = userRepository.findById(id).get();
            // verifica email //
            ////////////////////
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);

            // success message
            Map<String, Object> response = Map.of(
                    "message", "Senha trocada com sucesso"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "senha invalida"));
        }
    }

    public ResponseEntity<?> deleteUser(Long id) {
        try {
            userRepository.deleteById(id);

            // success message
            Map<String, Object> response = Map.of(
                    "message", "User deletado"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "User não encontrado, ou não autorizado"));
        }
    }

    public ResponseEntity<?> getUserById(Long id) {
        try {
            if (userRepository.findById(id).isEmpty()){
                return ResponseEntity.status(401).body(Map.of("error", "user não encontrado"));
            }
            User user = userRepository.findById(id).get();

            // success message
            Map<String, Object> response = Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName()
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "User não encontrado"));
        }
    }

    public ResponseEntity<?> listUsers() {
        try {
            List<User> users = userRepository.findAll();

            Map<String, Object> response = new HashMap<>();

            for (User user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());

                response.put(user.getName(), userInfo);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao buscar usuários"));
        }
    }


    public ResponseEntity<?> getByName(String name) {
        try {
            List<User> users = userRepository.findAllByName(name);

            Map<String, Object> response = new HashMap<>();

            for (User user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());

                response.put(user.getName(), userInfo);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao buscar usuários"));
        }
    }

}
