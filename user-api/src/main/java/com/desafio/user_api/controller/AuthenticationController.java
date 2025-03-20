package com.desafio.user_api.controller;

import com.desafio.user_api.service.authentication.DataAuthentication;
import com.desafio.user_api.service.forgotPassword.DataEmail;
import com.desafio.user_api.service.forgotPassword.DataforResetPassword;
import com.desafio.user_api.service.register.DataRegister;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    // to authentic with login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid DataAuthentication data) {
        return ResponseEntity.ok(true);
    }

    // register user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid DataRegister data) {
        return ResponseEntity.ok(true);
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
