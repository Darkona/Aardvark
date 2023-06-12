package com.darkona.aardvark.domain;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class Login {

    @NotNull
    @Email(message = "Must respect correct email format: xxxxx@xxx.xxx")
    private String email;

    @NotNull
    @Pattern(regexp = "^(?=.*\\d.*\\d)(?=.*[A-Z])(?!.*[^a-zA-Z0-9])(?=(?:.*[A-Z]){1})(?=(?:[^A-Z]*[a-z]){0,}[A-Za-z0-9]).{8,12}$",
            message = "validation.password")
    private String password;
}
