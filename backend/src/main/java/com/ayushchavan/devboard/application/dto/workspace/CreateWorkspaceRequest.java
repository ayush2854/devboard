package com.ayushchavan.devboard.application.dto.workspace;

public class CreateWorkspaceRequest {

    private String name;
    private String description;

    public CreateWorkspaceRequest() {
        // Required for JSON deserialization
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}