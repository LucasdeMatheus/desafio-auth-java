package com.desafio.userapi.service.email;

import com.desafio.userapi.service.user.CodeService;
import com.desafio.userapi.service.user.UserDTO;
import com.myproject.sendEmails.email.EmailSender;
import com.myproject.sendEmails.email.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class EmailService {

    @Autowired
    EmailSender emailSender;

    @Autowired
    private CodeService codeService;
    public boolean validateEmail(UserDTO userData, Type type) throws IOException {
        String code =  codeService.getCode();
        Map<String, String> data = new HashMap<>();
        data.put("nome", userData.name());
        data.put("codigo", code);
        emailSender.sendTextEmail(new ArrayList<>(List.of(userData.email())), type, new Date(), data);
        codeService.storeCode(userData.email(), code);
        return true;
    }


    public void sendSucess(String email, String name, Type type) throws IOException {
        Map<String, String> data = new HashMap<>();
        data.put("nome", name);
        emailSender.sendTextEmail(new ArrayList<>(List.of(email)), type, new Date(), data);
    }
}
