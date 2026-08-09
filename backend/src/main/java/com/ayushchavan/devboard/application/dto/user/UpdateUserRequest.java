package com.ayushchavan.devboard.application.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

@NotBlank(message = "Name is required")
@Size(max = 100, message = "Name must not exceed 100 characters")
private String name;

@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
@Size(max = 254, message = "Email must not exceed 254 characters")
private String email;

public UpdateUserRequest() {
    // Required for JSON deserialization
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}

}
