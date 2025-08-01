package com.desafio.userapi.controller;

import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import com.desafio.userapi.service.authentication.TypeUser;
import com.desafio.userapi.service.client.ClientService;
import com.desafio.userapi.service.email.ConfirmEmailDTO;
import com.desafio.userapi.service.user.UserDTO;
import com.desafio.userapi.service.user.UserService;
import com.desafio.userapi.service.authentication.AuthenticationService;
import com.desafio.userapi.service.authentication.DataAuthentication;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserService userService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientService clientService;

    // to authentic with login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody DataAuthentication data, HttpSession session) {
            return (ResponseEntity<Map<String, Object>>) authenticationService.login(data, session);
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


    @GetMapping("/authorize")
    public ResponseEntity<?> authorize(@RequestParam("client_id") String clientId,
                                          @RequestParam("redirect_uri") String redirectUri,
                                          HttpSession session){

        Optional<Client> optionalClient = clientRepository.findByClientId(clientId);

        if (optionalClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Client ID inválido"));
        }

        Client client = optionalClient.get();


        if (!client.getRedirectUri().equals(redirectUri)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Redirect URI inválido"));
        }


        session.setAttribute("client_id", clientId);
        session.setAttribute("redirect_uri", redirectUri);


        URI loginUri = URI.create("/login");
        return ResponseEntity.status(HttpStatus.FOUND).location(loginUri).build();
    }

    @PostMapping("/token")
    private ResponseEntity<?> getToken(@RequestBody TokenDTO data){
        return clientService.handleGrantType(data);
    }
}