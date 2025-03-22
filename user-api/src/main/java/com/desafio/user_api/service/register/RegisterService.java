package com.desafio.user_api.service.register;

import com.desafio.user_api.domain.User;
import com.desafio.user_api.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RegisterService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public ResponseEntity<?> registerUser(DataRegister data) {
        try {
            // check if the email already exists
            if (userRepository.existsByEmail(data.email())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email já está em uso"));
            }

            // create user in DB
            User user = new User();
            user.setEmail(data.email());
            String senhaCriptografada = passwordEncoder.encode(data.password());
            user.setPassword(senhaCriptografada);
            user.setName(data.name());
            userRepository.save(user);

            // JSON message
            Map<String, Object> response = Map.of(
                    "message", "Usuário criado com sucesso",
                    "user", Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail()
                    )
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DataIntegrityViolationException e) {
            // error in the DB
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Dados inválidos ou já cadastrados"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno no servidor"));
        }

    }
}
