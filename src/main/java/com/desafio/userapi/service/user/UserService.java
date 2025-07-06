package com.desafio.userapi.service.user;

import com.desafio.userapi.service.email.ConfirmEmailDTO;
import com.desafio.userapi.domain.User;
import com.desafio.userapi.domain.UserRepository;
import com.desafio.userapi.infra.TokenService;
import com.desafio.userapi.service.email.EmailService;
import com.myproject.sendEmails.email.Type;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private EmailService emailService;

    @Autowired
    private CodeService codeService;
    @Autowired
    private AuthenticationManager manager;
    public ResponseEntity<Map<String, Object>> register(UserDTO data) {
        try {
            if (userRepository.existsByEmail(data.email())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email já está em uso"));
            }

            emailService.validateEmail(data, Type.VALIDEMAIL);

            Map<String, Object> response = Map.of(
                    "message", "Código enviado para verificação no e-mail informado"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of("error", e.toString()));
        }

    }

    public ResponseEntity<?> updateEmail(String email, @AuthenticationPrincipal User user) {
        try {
            if (userRepository.existsByEmail(email)){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email já está em uso"));
            }

            emailService.validateEmail(new UserDTO(user.getName(), user.getEmail()), Type.UPEMAIL);

            codeService.getCodeMap().put("email-change", email);
            // success message
            Map<String, Object> response = Map.of(
                    "message", "Codigo de verificação enviado para email cadastrado"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Email já está em uso, ou não autorizado"));
        }
    }

    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal User user) {
        try {
            emailService.validateEmail(new UserDTO(user.getName(), user.getEmail()), Type.UPPASSWORD);

            Map<String, Object> response = Map.of(
                    "message", "Código de verificação  enviado para seu e-mail"
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "email invalido"));
        }
    }

    public ResponseEntity<?> deleteUser(User user) {
        try {
            emailService.validateEmail(new UserDTO(user.getName(), user.getEmail()), Type.DELETEUSER);


            // success message
            Map<String, Object> response = Map.of(
                    "message", "Email para confirmação de exclusão de usario enviado para o email cadastratado"
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

    public ResponseEntity<?> verifyEmail(ConfirmEmailDTO data) {
        try{
            if (!codeService.isCodeValid(data.userDTO().email(), data.code())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Código inválido"));
            }
            User user = null;

            switch (data.type()) {
                case VALIDEMAIL -> {
                    user = new User(data);
                    emailService.sendSucess(user.getEmail(), user.getName(), Type.WELLCOME);
                    user.setPassword(passwordEncoder.encode(data.password()));
                }
                case UPPASSWORD -> {
                    user = userRepository.findByEmail(data.userDTO().email());
                    emailService.sendSucess(user.getEmail(), user.getName(), Type.SUCESSCHANGEPASSWORD);
                    user.setPassword(passwordEncoder.encode(data.password()));
                }
                case UPEMAIL -> {
                    user = userRepository.findByEmail(data.userDTO().email());
                    user.setEmail(codeService.getCodeMap().get("email-change"));
                    emailService.sendSucess(user.getEmail(), user.getName(), Type.SUCESSCHANGEEMAIL);
                }
                case DELETEUSER -> {
                    emailService.sendSucess(data.userDTO().email(), data.userDTO().name(), Type.DELETESUCESSUSER);
                    userRepository.deleteByEmail(data.userDTO().email());
                    return ResponseEntity.ok("Usuario excluido com sucesso");
                }
            }
            userRepository.save(user);
            
            var authToken = new UsernamePasswordAuthenticationToken(user.getEmail(), data.password());
            var authentication = manager.authenticate(authToken);
            var token = tokenService.gerarToken((User) authentication.getPrincipal());

            // success message
            Map<String, Object> response = Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "token", token
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            e.printStackTrace();  // para ver detalhes no log
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao verificar codigo"));
        }

    }
}
