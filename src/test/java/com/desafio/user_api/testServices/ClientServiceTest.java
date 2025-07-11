package com.desafio.user_api.testServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import com.desafio.userapi.domain.user.User;
import com.desafio.userapi.domain.user.UserRepository;
import com.desafio.userapi.infra.TokenService;
import com.desafio.userapi.service.client.AuthorizationCodeService;
import com.desafio.userapi.service.client.ClientService;
import com.desafio.userapi.controller.TokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExtendWith(SpringExtension.class)
class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AuthorizationCodeService authorizationCodeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    private TokenDTO tokenDTO;

    @BeforeEach
    void setup() {
        tokenDTO = Mockito.mock(TokenDTO.class);
    }

    // --- handleClientCredentials ---

    @Test
    void handleClientCredentials_Success() {
        Client client = mock(Client.class);
        when(clientRepository.findByClientId("client123")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("secret", client.getClientSecret())).thenReturn(true);
        when(tokenService.gerarToken(client)).thenReturn("access-token-xyz");

        when(tokenDTO.client_id()).thenReturn("client123");
        when(tokenDTO.client_secret()).thenReturn("secret");

        ResponseEntity<?> response = clientService.handleClientCredentials(tokenDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("access-token-xyz", body.get("access_token"));
        assertEquals(3600, body.get("expires_in"));
    }

    @Test
    void handleClientCredentials_InvalidClientId() {
        when(clientRepository.findByClientId("invalid")).thenReturn(Optional.empty());
        when(tokenDTO.client_id()).thenReturn("invalid");

        ResponseEntity<?> response = clientService.handleClientCredentials(tokenDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((Map<?, ?>)response.getBody()).containsKey("error"));
    }

    @Test
    void handleClientCredentials_InvalidClientSecret() {
        Client client = mock(Client.class);
        when(clientRepository.findByClientId("client123")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("wrongsecret", client.getClientSecret())).thenReturn(false);

        when(tokenDTO.client_id()).thenReturn("client123");
        when(tokenDTO.client_secret()).thenReturn("wrongsecret");

        ResponseEntity<?> response = clientService.handleClientCredentials(tokenDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((Map<?, ?>)response.getBody()).containsKey("error"));
    }

    // --- handleAuthorizationCode ---

    @Test
    void handleAuthorizationCode_Success() {
        Client client = mock(Client.class);
        when(clientRepository.findByClientId("client123")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("secret", client.getClientSecret())).thenReturn(true);

        when(tokenDTO.client_id()).thenReturn("client123");
        when(tokenDTO.client_secret()).thenReturn("secret");
        when(tokenDTO.code()).thenReturn("code123");

        when(authorizationCodeService.getTokenByCode("code123")).thenReturn("tokenXYZ");
        when(authorizationCodeService.createCode("tokenXYZ")).thenReturn("refreshToken123"); // <-- stub necessário

        ResponseEntity<?> response = clientService.handleAuthorizationCode(tokenDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("tokenXYZ", body.get("access_token"));
        assertEquals("refreshToken123", body.get("refresh_token"));
        verify(authorizationCodeService).removeCode("code123");
        verify(authorizationCodeService).createCode("tokenXYZ");
    }


    @Test
    void handleAuthorizationCode_InvalidClient() {
        when(clientRepository.findByClientId("client123")).thenReturn(Optional.empty());

        when(tokenDTO.client_id()).thenReturn("client123");

        ResponseEntity<?> response = clientService.handleAuthorizationCode(tokenDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleAuthorizationCode_InvalidCode() {
        Client client = mock(Client.class);
        when(clientRepository.findByClientId("client123")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        when(tokenDTO.client_id()).thenReturn("client123");
        when(tokenDTO.client_secret()).thenReturn("secret");
        when(tokenDTO.code()).thenReturn("code123");

        when(authorizationCodeService.getTokenByCode("code123")).thenReturn(null);

        ResponseEntity<?> response = clientService.handleAuthorizationCode(tokenDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // --- handleRefreshToken ---

    @Test
    void handleRefreshToken_Success() {
        Client client = mock(Client.class);
        User user = mock(User.class);

        when(clientRepository.findByClientId("client123")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("secret", client.getClientSecret())).thenReturn(true);

        when(tokenDTO.client_id()).thenReturn("client123");
        when(tokenDTO.client_secret()).thenReturn("secret");
        when(tokenDTO.code()).thenReturn("oldCode");

        when(authorizationCodeService.getTokenByCode("oldCode")).thenReturn("oldToken");

        when(tokenService.getSubject("oldToken")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(client.getScopes()).thenReturn(Set.of());

        when(tokenService.gerarToken(user, Set.of())).thenReturn("newToken");
        when(authorizationCodeService.createCode("newToken")).thenReturn("newCode");

        ResponseEntity<?> response = clientService.handleRefreshToken(tokenDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("newToken", body.get("access_token"));
        assertEquals("newCode", body.get("refresh_token"));
        verify(authorizationCodeService).removeCode("oldCode");
    }

    @Test
    void handleRefreshToken_InvalidClient() {
        when(clientRepository.findByClientId("invalid")).thenReturn(Optional.empty());

        when(tokenDTO.client_id()).thenReturn("invalid");

        ResponseEntity<?> response = clientService.handleRefreshToken(tokenDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleRefreshToken_InvalidCode() {
        Client client = mock(Client.class);
        when(clientRepository.findByClientId("client123")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        when(tokenDTO.client_id()).thenReturn("client123");
        when(tokenDTO.client_secret()).thenReturn("secret");
        when(tokenDTO.code()).thenReturn("oldCode");

        when(authorizationCodeService.getTokenByCode("oldCode")).thenReturn(null);

        ResponseEntity<?> response = clientService.handleRefreshToken(tokenDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
