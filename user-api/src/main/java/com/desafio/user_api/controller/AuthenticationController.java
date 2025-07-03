package com.desafio.user_api.controller;

import com.desafio.user_api.domain.UserRepository;
import com.desafio.user_api.service.user.UserDTO;
import com.desafio.user_api.service.user.UserService;
import com.desafio.user_api.infra.TokenService;
import com.desafio.user_api.service.authentication.AuthenticationService;
import com.desafio.user_api.service.authentication.DataAuthentication;
import com.desafio.user_api.service.forgotPassword.DataEmail;
import com.desafio.user_api.service.forgotPassword.DataforResetPassword;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    private UserService userService;




    // to authentic with login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody DataAuthentication data) {
            return authenticationService.login(data);
    }

    // register user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserDTO data) {
        return userService.register(data);
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