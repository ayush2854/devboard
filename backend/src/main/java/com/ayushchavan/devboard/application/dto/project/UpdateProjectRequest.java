package com.ayushchavan.devboard.application.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProjectRequest {

@NotBlank(message = "Project name is required")
@Size(max = 100, message = "Project name must not exceed 100 characters")
private String name;

@Size(max = 1000, message = "Description must not exceed 1000 characters")
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
