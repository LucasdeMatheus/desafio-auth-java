package com.desafio.userapi.service.email;

import com.desafio.userapi.service.user.UserDTO;
import com.myproject.sendEmails.email.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmEmailDTO(UserDTO userDTO,
                              String code,
                              Type type,
                              @NotBlank
                              @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,11}$",
                                      message = "A senha deve ter entre 8 e 11 caracteres, incluir pelo menos uma letra maiúscula, uma minúscula e um caractere especial (@#$%^&+=)")
                              String password
                              ) {}

