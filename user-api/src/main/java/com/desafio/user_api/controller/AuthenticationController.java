package com.desafio.user_api.controller;

import com.desafio.user_api.domain.User;
import com.desafio.user_api.domain.UserRepository;
import com.desafio.user_api.infra.DataTokenJWT;
import com.desafio.user_api.infra.TokenService;
import com.desafio.user_api.service.authentication.AuthenticationService;
import com.desafio.user_api.service.authentication.DataAuthentication;
import com.desafio.user_api.service.forgotPassword.DataEmail;
import com.desafio.user_api.service.forgotPassword.DataforResetPassword;
import com.desafio.user_api.service.register.DataRegister;
import com.desafio.user_api.service.register.RegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager manager;



    // to authentic with login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody DataAuthentication data) {
        try {
            String email = data.email();

            // check user in DB
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
            }

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

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }

    }



    @Autowired
    private RegisterService registerService;

    // register user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid DataRegister data) {
        return registerService.registerUser(data);
    }

    // to authentic/register user with GOOGLE

    /////////////////////////////////////////////////////////////

    // valid email for to reset password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPasword(@RequestBody @Valid DataEmail data) {
        return ResponseEntity.ok(true);
    }

    // reset password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPasword(@RequestBody @Valid DataforResetPassword data) {
        return ResponseEntity.ok(true);
    }
}
