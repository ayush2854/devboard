package com.ayushchavan.devboard.application.dto.user;

public class UpdateUserRequest {

    private String name;
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