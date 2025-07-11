package com.desafio.user_api.testServices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.desafio.userapi.domain.client.Client;
import com.desafio.userapi.domain.client.ClientRepository;
import com.desafio.userapi.domain.user.User;
import com.desafio.userapi.domain.user.UserRepository;
import com.desafio.userapi.service.authentication.AuthenticationService;
import com.desafio.userapi.service.authentication.DataAuthentication;
import com.desafio.userapi.service.authentication.TypeUser;
import com.desafio.userapi.service.email.ConfirmEmailDTO;
import com.desafio.userapi.service.email.EmailService;
import com.desafio.userapi.service.user.CodeService;
import com.desafio.userapi.service.user.UserDTO;
import com.desafio.userapi.service.user.UserService;
import com.myproject.sendEmails.email.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Map;

@ExtendWith(SpringExtension.class)
public class UserServiceConfirmEmailTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private CodeService codeService;

    @Mock
    private AuthenticationService authenticationService;

    private static final String VALID_PASSWORD = "Abcdef@1"; // corresponde ao regex

    private final UserDTO userDTO = new UserDTO("Test User", "user@example.com");

    @BeforeEach
    void setup() {
        Mockito.reset(userRepository, clientRepository, passwordEncoder, emailService, codeService, authenticationService);
    }

    // Helper para criar ConfirmEmailDTO
    private ConfirmEmailDTO makeConfirmEmailDTO(Type type, TypeUser typeUser) {
        return new ConfirmEmailDTO(userDTO, "code123", type, VALID_PASSWORD, typeUser);
    }

    @Test
    void verifyEmail_InvalidCode_ReturnsBadRequest() {
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.VALIDEMAIL, TypeUser.USER);
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(false);

        ResponseEntity<?> response = userService.verifyEmail(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    void verifyEmail_ValidEmailRegistration_User_Success() throws IOException {
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.VALIDEMAIL, TypeUser.USER);
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(true);

        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("encodedPass");
        doNothing().when(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.WELLCOME);

        Map<String, String> tokenMap = Map.of("token", "jwt-token");
        ResponseEntity<Map<String, String>> loginResponse = ResponseEntity.ok(tokenMap);
        when(authenticationService.login(any(), isNull())).thenReturn((ResponseEntity) loginResponse);

        // mock do save para verificar senha codificada
        doAnswer(invocation -> {
            User argUser = invocation.getArgument(0);
            assertEquals("encodedPass", argUser.getPassword());
            return argUser;
        }).when(userRepository).save(any(User.class));

        ResponseEntity<?> response = userService.verifyEmail(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", ((Map<?, ?>) response.getBody()).get("token"));

        verify(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.WELLCOME);
        verify(userRepository).save(any(User.class));
        verify(authenticationService).login(any(), isNull());
    }

    @Test
    void verifyEmail_ValidEmailRegistration_Client_Success() throws IOException {
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.VALIDEMAIL, TypeUser.CLIENT);
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(true);

        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("encodedPass");
        doNothing().when(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.WELLCOME);

        Map<String, String> tokenMap = Map.of("token", "jwt-token");
        ResponseEntity<Map<String, String>> loginResponse = ResponseEntity.ok(tokenMap);
        when(authenticationService.login(any(), isNull())).thenReturn((ResponseEntity) loginResponse);

        doAnswer(invocation -> {
            Client argClient = invocation.getArgument(0);
            assertEquals("encodedPass", argClient.getClientSecret());
            return argClient;
        }).when(clientRepository).save(any(Client.class));

        ResponseEntity<?> response = userService.verifyEmail(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", ((Map<?, ?>) response.getBody()).get("token"));

        verify(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.WELLCOME);
        verify(clientRepository).save(any(Client.class));
        verify(authenticationService).login(any(), isNull());
    }

    @Test
    void verifyEmail_UpdatePassword_User_Success() throws IOException {
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.UPPASSWORD, TypeUser.USER);
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(true);

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(userDTO.email());
        when(user.getName()).thenReturn(userDTO.name());

        when(userRepository.findByEmail(userDTO.email())).thenReturn(user);
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("encodedPass");
        doNothing().when(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.SUCESSCHANGEPASSWORD);

        Map<String, String> tokenMap = Map.of("token", "jwt-token");
        ResponseEntity<Map<String, String>> loginResponse = ResponseEntity.ok(tokenMap);
        when(authenticationService.login(any(), isNull())).thenReturn((ResponseEntity) loginResponse);

        ResponseEntity<?> response = userService.verifyEmail(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", ((Map<?, ?>) response.getBody()).get("token"));

        verify(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.SUCESSCHANGEPASSWORD);
        verify(authenticationService).login(any(), isNull());
    }

    @Test
    void verifyEmail_UpdatePassword_Client_Success() throws IOException {
        // Cria o DTO com tipo de atualização de senha e usuário do tipo CLIENT
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.UPPASSWORD, TypeUser.CLIENT);

        // Simula que o código é válido
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(true);

        // Usa uma instância real de Client, não mock
        Client client = new Client();
        client.setEmail(userDTO.email());
        client.setClientId(userDTO.name());

        // Quando o repositório buscar o client pelo email, retorna essa instância real
        when(clientRepository.findByEmail(userDTO.email())).thenReturn(client);

        // Simula o encode da senha
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("encodedPass");

        // Simula envio do e-mail de sucesso (não faz nada)
        doNothing().when(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.SUCESSCHANGEPASSWORD);

        // Intercepta o save para verificar que o clientSecret foi setado com "encodedPass"
        doAnswer(invocation -> {
            Client argClient = invocation.getArgument(0);
            assertEquals("encodedPass", argClient.getClientSecret());
            return argClient;
        }).when(clientRepository).save(any(Client.class));

        // Mocka o login para retornar um token JWT
        Map<String, String> tokenMap = Map.of("token", "jwt-token");
        ResponseEntity<Map<String, String>> loginResponse = ResponseEntity.ok(tokenMap);
        when(authenticationService.login(any(), isNull())).thenReturn((ResponseEntity) loginResponse);

        // Chama o método real a ser testado
        ResponseEntity<?> response = userService.verifyEmail(dto);

        // Valida o resultado do método
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", ((Map<?, ?>) response.getBody()).get("token"));

        // Verifica se os métodos foram chamados corretamente
        verify(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.SUCESSCHANGEPASSWORD);
        verify(clientRepository).save(any(Client.class));
        verify(authenticationService).login(any(), isNull());
    }


    @Test
    void verifyEmail_UpdateEmail_User_Success() throws IOException {
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.UPPASSWORD, TypeUser.USER);
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(true);

        // Usa uma instância real de User, não mock
        User user = new User(new UserDTO(userDTO.name(), userDTO.email()));
        user.setEmail(userDTO.email());
        user.setName(userDTO.name());

        // Quando o repositório buscar o user pelo email, retorna essa instância real
        when(userRepository.findByEmail(userDTO.email())).thenReturn(user);

        // Simula o encode da senha
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("encodedPass");

        // Simula envio do e-mail de sucesso (não faz nada)
        doNothing().when(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.SUCESSCHANGEPASSWORD);

        // Intercepta o save para verificar que a senha foi setada corretamente
        doAnswer(invocation -> {
            User argUser = invocation.getArgument(0);
            assertEquals("encodedPass", argUser.getPassword());
            return argUser;
        }).when(userRepository).save(any(User.class));

        // Mocka o login para retornar um token JWT
        Map<String, String> tokenMap = Map.of("token", "jwt-token");
        ResponseEntity<Map<String, String>> loginResponse = ResponseEntity.ok(tokenMap);
        when(authenticationService.login(any(), isNull())).thenReturn((ResponseEntity) loginResponse);

        // Chama o método real a ser testado
        ResponseEntity<?> response = userService.verifyEmail(dto);

        // Valida o resultado do método
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", ((Map<?, ?>) response.getBody()).get("token"));

        // Verifica se os métodos foram chamados corretamente
        verify(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.SUCESSCHANGEPASSWORD);
        verify(userRepository).save(any(User.class));
        verify(authenticationService).login(any(), isNull());
    }





    @Test
    void verifyEmail_UpdateEmail_Client_Success() throws IOException {
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.UPPASSWORD, TypeUser.CLIENT);
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(true);

        // Usa uma instância real de Client, não mock
        Client client = new Client();
        client.setEmail(userDTO.email());
        client.setClientId(userDTO.name());

        // Quando o repositório buscar o client pelo email, retorna essa instância real
        when(clientRepository.findByEmail(userDTO.email())).thenReturn(client);

        // Simula o encode da senha
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("encodedPass");

        // Simula envio do e-mail de sucesso (não faz nada)
        doNothing().when(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.SUCESSCHANGEPASSWORD);

        // Intercepta o save para verificar que o clientSecret foi setado com "encodedPass"
        doAnswer(invocation -> {
            Client argClient = invocation.getArgument(0);
            assertEquals("encodedPass", argClient.getClientSecret());
            return argClient;
        }).when(clientRepository).save(any(Client.class));

        // Mocka o login para retornar um token JWT
        Map<String, String> tokenMap = Map.of("token", "jwt-token");
        ResponseEntity<Map<String, String>> loginResponse = ResponseEntity.ok(tokenMap);
        when(authenticationService.login(any(), isNull())).thenReturn((ResponseEntity) loginResponse);

        // Chama o método real a ser testado
        ResponseEntity<?> response = userService.verifyEmail(dto);

        // Valida o resultado do método
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", ((Map<?, ?>) response.getBody()).get("token"));

        // Verifica se os métodos foram chamados corretamente
        verify(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.SUCESSCHANGEPASSWORD);
        verify(clientRepository).save(any(Client.class));
        verify(authenticationService).login(any(), isNull());
    }

    @Test
    void verifyEmail_DeleteUser_User_Success() throws IOException {
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.DELETEUSER, TypeUser.USER);
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(true);

        doNothing().when(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.DELETESUCESSUSER);
        doNothing().when(userRepository).deleteByEmail(userDTO.email());

        ResponseEntity<?> response = userService.verifyEmail(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Usuario excluido com sucesso", response.getBody());

        verify(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.DELETESUCESSUSER);
        verify(userRepository).deleteByEmail(userDTO.email());
    }

    @Test
    void verifyEmail_DeleteUser_Client_Success() throws IOException {
        ConfirmEmailDTO dto = makeConfirmEmailDTO(Type.DELETEUSER, TypeUser.CLIENT);
        when(codeService.isCodeValid(userDTO.email(), "code123")).thenReturn(true);

        doNothing().when(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.DELETESUCESSUSER);
        doNothing().when(clientRepository).deleteByEmail(userDTO.email());

        ResponseEntity<?> response = userService.verifyEmail(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Client excluido com sucesso", response.getBody());

        verify(emailService).sendSucess(userDTO.email(), userDTO.name(), Type.DELETESUCESSUSER);
        verify(clientRepository).deleteByEmail(userDTO.email());
    }
}
