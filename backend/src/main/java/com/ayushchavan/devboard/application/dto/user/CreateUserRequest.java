package com.ayushchavan.devboard.application.dto.user;

public class CreateUserRequest {

    private String name;
    private String email;
    private String password;

    public CreateUserRequest() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}