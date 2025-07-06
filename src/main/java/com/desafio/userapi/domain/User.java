package com.desafio.userapi.domain;

import com.desafio.userapi.service.email.ConfirmEmailDTO;
import com.desafio.userapi.service.user.UserDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    public User(String email, String name) {
    }



    public User(UserDTO data) {
        this.email = data.email();
        this.name = data.name();
    }

    public User(ConfirmEmailDTO data) {
        this.email = data.userDTO().email();
        this.name = data.userDTO().name();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }



    public void setPassword(String password) {
        this.password = password;
    }
}
