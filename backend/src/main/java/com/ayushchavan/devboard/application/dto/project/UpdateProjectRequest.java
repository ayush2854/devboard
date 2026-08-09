package com.ayushchavan.devboard.application.dto.project;

public class UpdateProjectRequest {

    private String name;
    private String description;

    public UpdateProjectRequest() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}