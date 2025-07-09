package com.desafio.userapi.controller;

import com.desafio.userapi.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class OAuth2Controller {
    @Autowired
    private UserService userService;
    @GetMapping("/oauth-success")
    public ResponseEntity<?> success(OAuth2AuthenticationToken auth) throws IOException {
        Map<String, Object> attributes = auth.getPrincipal().getAttributes();

        return userService.loginOAuth2(attributes);
    }

}
