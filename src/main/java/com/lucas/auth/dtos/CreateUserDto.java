package com.lucas.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDto (
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
){
}
