package com.desafio.userapi.domain.client;

import com.desafio.userapi.service.client.GrantType;
import com.desafio.userapi.service.client.Scope;
import com.desafio.userapi.service.email.ConfirmEmailDTO;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
public class Client implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;

    private String clientSecret;

    @Column(nullable = false)
    private boolean oauthUser = false;

    private String redirectUri;

    @ElementCollection
    private Set<Scope> scopes;

    @ElementCollection
    private Set<GrantType> grantTypes;

    private String email;



    public Client() {
    }

    public Client(ConfirmEmailDTO data) {
        this.email = data.userDTO().email();
        this.clientId = data.userDTO().name();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    public Set<GrantType> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(Set<GrantType> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isOauthUser() {
        return oauthUser;
    }

    public void setOauthUser(boolean oauthUser) {
        this.oauthUser = oauthUser;
    }

    // ---------------------- MÉTODOS UserDetails -----------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Aqui você pode retornar roles, scopes ou authorities se quiser
        return List.of(); // vazio por enquanto
    }

    @Override
    public String getPassword() {
        // O clientSecret será tratado como senha
        return clientSecret;
    }

    @Override
    public String getUsername() {
        // Você pode usar email ou clientId para login, aqui está o email
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Se quiser implementar lógica para expiração, faça aqui
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Se quiser implementar bloqueio, faça aqui
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Se quiser implementar expiração de credencial, faça aqui
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Pode ter campo para habilitar/desabilitar cliente, por enquanto true
        return true;
    }
}
