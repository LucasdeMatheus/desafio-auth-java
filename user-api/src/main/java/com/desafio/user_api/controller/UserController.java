package com.desafio.user_api.controller;

import com.desafio.user_api.domain.User;
import com.desafio.user_api.service.user.UpdateEmailDTO;
import com.desafio.user_api.service.user.UpdatePasswordDTO;
import com.desafio.user_api.service.user.UserService;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/update-email/{id}")
    public ResponseEntity<?> updateEmail(@PathVariable("id") Long id, @Valid @RequestBody UpdateEmailDTO data, @AuthenticationPrincipal User user) {
        // valida usuario
        if (user.getId() != id) {
            return ResponseEntity.status(403).body(Map.of("error", "Acesso negado"));
        }
        return userService.updateEmail(data.email(), id);
    }

    @PutMapping("/update-password/{id}")
    public ResponseEntity<?> updatePassword(@PathVariable("id") Long id, @Valid @RequestBody UpdatePasswordDTO data, @AuthenticationPrincipal User user) {
        // valida usuario
        if (user.getId() != id) {
            return ResponseEntity.status(403).body(Map.of("error", "Acesso negado"));
        }
        return userService.updatePassword(data.password(), id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id, @AuthenticationPrincipal User user) {
        // valida usuario
        if (user.getId() != id) {
            return ResponseEntity.status(403).body(Map.of("error", "Acesso negado"));
        }
        return userService.deleteUser(id);
    }
    @GetMapping("/get-id/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id){
        return userService.getUserById(id);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listUsers(){
        return userService.listUsers();
    }

    @GetMapping("/get-name/{name}")
    public ResponseEntity<?> getByName(@PathVariable("name") String name){
        return userService.getByName(name);
    }
}
