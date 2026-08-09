package com.ayushchavan.devboard.application.dto.workspace;

import com.ayushchavan.devboard.domain.entity.WorkspaceRole;

public class UpdateWorkspaceMemberRoleRequest {

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