package com.desafio.userapi.service.user;

import com.desafio.userapi.controller.UPpasswordDTO;
import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import com.desafio.userapi.domain.user.User;
import com.desafio.userapi.domain.user.UserRepository;
import com.desafio.userapi.infra.TokenService;
import com.desafio.userapi.service.authentication.AuthenticationService;
import com.desafio.userapi.service.authentication.DataAuthentication;
import com.desafio.userapi.service.authentication.TypeUser;
import com.desafio.userapi.service.email.ConfirmEmailDTO;
import com.desafio.userapi.service.email.EmailService;
import com.myproject.sendEmails.email.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService    {
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
    private ClientRepository clientRepository;


    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ApplicationContext applicationContext;

    public ResponseEntity<Map<String, Object>> register(UserDTO data) {
        try {
            if (userRepository.existsByEmail(data.email()) || clientRepository.existsByEmail(data.email())){
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

    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UPpasswordDTO uPpasswordDTO) {
        try {
            User user = null;
            Client client = null;
            if (uPpasswordDTO.typeUser().equals(TypeUser.USER)) {
                user = userRepository.findByEmail(uPpasswordDTO.email());
                emailService.validateEmail(new UserDTO(user.getName(), user.getEmail()), Type.UPPASSWORD);

            }else{
                client = clientRepository.findByEmail(uPpasswordDTO.email());
                emailService.validateEmail(new UserDTO(client.getClientId(), client.getEmail()), Type.UPPASSWORD);
            }

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
        try {
            if (!codeService.isCodeValid(data.userDTO().email(), data.code())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Código inválido"));
            }

            User user = null;
            Client client = null;


            // Operações do switch que atualizam/criam usuário ou client
            switch (data.type()) {
                case VALIDEMAIL -> {
                    if (data.typeUser().equals(TypeUser.USER)) {
                        user = new User(data);
                        emailService.sendSucess(user.getEmail(), user.getName(), Type.WELLCOME);
                        user.setPassword(passwordEncoder.encode(data.password()));

                    } else {
                        client = new Client(data);
                        emailService.sendSucess(client.getEmail(), client.getClientId(), Type.WELLCOME);
                        client.setClientSecret(passwordEncoder.encode(data.password()));
                    }
                }
                case UPPASSWORD -> {
                    if (data.typeUser().equals(TypeUser.USER)) {
                        user = userRepository.findByEmail(data.userDTO().email());
                        System.out.println(data.password());
                        emailService.sendSucess(user.getEmail(), user.getName(), Type.SUCESSCHANGEPASSWORD);
                        user.setPassword(passwordEncoder.encode(data.password()));

                    } else {
                        System.out.println("não entra aqui tambem");
                        client = clientRepository.findByEmail(data.userDTO().email());
                        emailService.sendSucess(client.getEmail(), client.getClientId(), Type.SUCESSCHANGEPASSWORD);
                        client.setClientSecret(passwordEncoder.encode(data.password()));

                    }
                }
                case UPEMAIL -> {
                    if (data.typeUser().equals(TypeUser.USER)) {
                        user = userRepository.findByEmail(data.userDTO().email());
                        user.setEmail(codeService.getCodeMap().get("email-change"));

                        emailService.sendSucess(user.getEmail(), user.getName(), Type.SUCESSCHANGEEMAIL);
                    } else {
                        client = clientRepository.findByEmail(data.userDTO().email());
                        client.setEmail(codeService.getCodeMap().get("email-change"));

                        emailService.sendSucess(client.getEmail(), client.getClientId(), Type.SUCESSCHANGEEMAIL);
                    }
                }
                case DELETEUSER -> {
                    if (data.typeUser().equals(TypeUser.USER)) {
                        emailService.sendSucess(data.userDTO().email(), data.userDTO().name(), Type.DELETESUCESSUSER);
                        userRepository.deleteByEmail(data.userDTO().email());
                        return ResponseEntity.ok("Usuario excluido com sucesso");
                    } else {
                        emailService.sendSucess(data.userDTO().email(), data.userDTO().name(), Type.DELETESUCESSUSER);
                        clientRepository.deleteByEmail(data.userDTO().email());
                        return ResponseEntity.ok("Client excluido com sucesso");
                    }
                }
            }

            // Agora chama o login, com a certeza que user ou client não é nulo
            if (data.typeUser().equals(TypeUser.USER)) {

                if (user == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuário não encontrado"));
                }

                userRepository.save(user);
                return authenticationService.login(new DataAuthentication(user.getEmail(), data.password(), data.typeUser()), null);
            } else {
                if (client == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cliente não encontrado"));
                }
                clientRepository.save(client);
                return authenticationService.login(new DataAuthentication(client.getEmail(), data.password(), data.typeUser()), null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao verificar codigo"));
        }
    }


    public ResponseEntity<?> loginOAuth2(Map<String, Object> attributes) throws IOException {
        String email = String.valueOf(attributes.get("email"));
        User user = userRepository.findByEmail(email);

        if (user == null) {
            user = new User(new UserDTO(
                    String.valueOf(attributes.get("name")),
                    email));
            user.setOauthUser(true);
            userRepository.save(user);
            emailService.sendSucess(email, user.getName(), Type.WELLCOME);
        }

        String token = tokenService.gerarToken(user, null);

        Map<String, Object> response = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "token", token
        );

        return ResponseEntity.ok(response);
    }

}
