package com.lucas.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDto (
        String username,
        @Email String email,
        @Size(min = 6) String password
){
}
