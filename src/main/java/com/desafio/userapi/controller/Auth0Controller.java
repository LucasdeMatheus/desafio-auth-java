package com.desafio.userapi.controller;

import com.desafio.userapi.service.email.ConfirmEmailDTO;
import com.desafio.userapi.service.user.UserDTO;
import com.desafio.userapi.service.user.UserService;
import com.desafio.userapi.service.authentication.AuthenticationService;
import com.desafio.userapi.service.authentication.DataAuthentication;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class Auth0Controller {

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

    @PostMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(@RequestBody ConfirmEmailDTO data) throws IOException {
        return userService.verifyEmail(data);
    }

    // to authentic/register user with GOOGLE

    /////////////////////////////////////////////////////////////
}