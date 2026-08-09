package com.ayushchavan.devboard.application.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateWorkspaceRequest {

@NotBlank(message = "Workspace name is required")
@Size(max = 100, message = "Workspace name must not exceed 100 characters")
private String name;

@Size(max = 1000, message = "Description must not exceed 1000 characters")
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
