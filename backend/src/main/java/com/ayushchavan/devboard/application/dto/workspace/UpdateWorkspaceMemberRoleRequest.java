package com.ayushchavan.devboard.application.dto.workspace;

import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

import jakarta.validation.constraints.NotNull;

public class UpdateWorkspaceMemberRoleRequest {

@NotNull(message = "Workspace role is required")
private WorkspaceRole role;

public UpdateWorkspaceMemberRoleRequest() {
    // Required for JSON deserialization
}

public WorkspaceRole getRole() {
    return role;
}

public void setRole(WorkspaceRole role) {
    this.role = role;
}

}
