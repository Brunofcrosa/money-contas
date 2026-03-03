package com.moneycontas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "name is required") @Size(min = 2, max = 255, message = "name must be between 2 and 255 characters") String name,

        @NotBlank(message = "email is required") @jakarta.validation.constraints.Email(message = "email must be a valid email address") String email,

        @NotBlank(message = "password is required") @Size(min = 8, message = "password must have at least 8 characters") String password

) {
}
