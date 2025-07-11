package com.desafio.userapi.controller;

import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.service.client.ClientService;
import com.desafio.userapi.service.client.ConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/config/{id}")
    public ResponseEntity<?> config(@PathVariable("id") Long id, @RequestBody ConfigDTO data, @AuthenticationPrincipal Client client){
        // valida client
        if (client.getId() != id) {
            return ResponseEntity.status(403).body(Map.of("error", "Acesso negado"));
        }
        return clientService.config(data, client);
    }


}
