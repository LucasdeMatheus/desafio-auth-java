package com.desafio.user_api.testServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import com.desafio.userapi.domain.user.User;
import com.desafio.userapi.domain.user.UserRepository;
import com.desafio.userapi.infra.TokenService;
import com.desafio.userapi.service.authentication.AuthenticationService;
import com.desafio.userapi.service.authentication.DataAuthentication;
import com.desafio.userapi.service.authentication.TypeUser;
import com.desafio.userapi.service.client.AuthorizationCodeService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorizationCodeService authorizationCodeService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSession session;

    private DataAuthentication dataUser;
    private DataAuthentication dataClient;

    @BeforeEach
    void setup() {
        dataUser = new DataAuthentication("user@example.com", "password", TypeUser.USER);
        dataClient = new DataAuthentication("client@example.com", "password", TypeUser.CLIENT);
    }

    // login padrão
    @Test
    void testLoginUserSuccess_NoClientSession() {
        User user = mock(User.class);
        when(userRepository.findByEmail("user@example.com")).thenReturn(user);
        when(user.isOauthUser()).thenReturn(false);
        when(session.getAttribute("client_id")).thenReturn(null);
        when(applicationContext.getBean(AuthenticationManager.class)).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, null));
        when(tokenService.gerarToken(user, null)).thenReturn("jwt-token");

        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getName()).thenReturn("User Test");

        ResponseEntity<?> response = authenticationService.login(dataUser, session);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("jwt-token", body.get("token"));
    }


    // login com oauth true
    @Test
    void testLoginUserFail_OauthUser() {
        User user = mock(User.class);
        when(userRepository.findByEmail("user@example.com")).thenReturn(user);
        when(user.isOauthUser()).thenReturn(true);

        ResponseEntity<?> response = authenticationService.login(dataUser, session);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((Map<?, ?>)response.getBody()).containsKey("error"));
    }

    // login com oauth
    @Test
    void testLoginClientSuccess_WithRedirectUri() {
        Client client = mock(Client.class);
        when(clientRepository.findByEmail("client@example.com")).thenReturn(client);
        when(client.isOauthUser()).thenReturn(false);
        when(passwordEncoder.matches("password", client.getClientSecret())).thenReturn(true);
        when(tokenService.gerarToken(client)).thenReturn("jwt-client-token");
        when(session.getAttribute("client_id")).thenReturn("client123");
        when(clientRepository.findByClientId("client123")).thenReturn(Optional.of(client));
        when(client.getRedirectUri()).thenReturn("http://redirect.uri");
        when(authorizationCodeService.createCode("jwt-client-token")).thenReturn("code123");

        ResponseEntity<?> response = authenticationService.login(dataClient, session);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        String body = (String) response.getBody();
        assertTrue(body.contains("http://redirect.uri?code=code123"));

        verify(session).removeAttribute("redirect_uri");
        verify(session).removeAttribute("client_id");
    }

    // login com senha errada
    @Test
    void testLoginClientFail_WrongPassword() {
        Client client = mock(Client.class);
        when(clientRepository.findByEmail("client@example.com")).thenReturn(client);
        when(client.isOauthUser()).thenReturn(false);
        when(passwordEncoder.matches("password", client.getClientSecret())).thenReturn(false);

        ResponseEntity<?> response = authenticationService.login(dataClient, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((Map<?, ?>)response.getBody()).containsKey("error"));
    }

    // busca user e client pelo email, se o retorno é certo-----------------
    @Test
    void testLoadUserByUsername_UserFound() {
        User user = mock(User.class);
        when(userRepository.findByEmail("user@example.com")).thenReturn(user);

        assertEquals(user, authenticationService.loadUserByUsername("user@example.com"));
    }

    @Test
    void testLoadUserByUsername_ClientFound() {
        Client client = mock(Client.class);
        when(clientRepository.findByEmail("client@example.com")).thenReturn(client);

        assertEquals(client, authenticationService.loadUserByUsername("client@example.com"));
    }

    // test para caso não retorne o user/client
    @Test
    void testLoadUserByUsername_NotFound() {
        when(clientRepository.findByEmail(anyString())).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        assertThrows(Exception.class, () -> authenticationService.loadUserByUsername("notfound@example.com"));
    }
}
