package com.desafio.user_api.domain;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    private String email;
    private String password;

    public User(String login, String password) {
    }
    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;  // Use the actual login
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Adjust as needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Adjust as needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Adjust as needed
    }

    @Override
    public boolean isEnabled() {
        return true;  // Adjust as needed
    }
    @Override
    public String toString() {
        return "User{" +
                ", login='" + email + '\'' +
                ", id=" + id +
                '}';
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
