CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE workspaces (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE workspace_memberships (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_workspace_memberships_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_workspace_memberships_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_workspace_memberships_user_workspace
        UNIQUE (workspace_id, user_id),

    CONSTRAINT chk_workspace_memberships_role
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER'))
);

CREATE TABLE teams (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_teams_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_teams_workspace_name
        UNIQUE (workspace_id, name)
);

CREATE TABLE team_memberships (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_team_memberships_team
        FOREIGN KEY (team_id)
        REFERENCES teams(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_team_memberships_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_team_memberships_team_user
        UNIQUE (team_id, user_id)
);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    archived_at TIMESTAMPTZ,

    CONSTRAINT fk_projects_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_projects_workspace_name
        UNIQUE (workspace_id, name),

    CONSTRAINT chk_projects_status
    CHECK (status IN ('ACTIVE', 'ARCHIVED')),

CONSTRAINT chk_projects_archive_consistency
    CHECK (
        (status = 'ACTIVE' AND archived_at IS NULL)
        OR
        (status = 'ARCHIVED' AND archived_at IS NOT NULL)
    )
);

CREATE TABLE project_memberships (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_project_memberships_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_project_memberships_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_project_memberships_project_user
        UNIQUE (project_id, user_id)
);


CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    assignee_id UUID,
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    archived_at TIMESTAMPTZ,

    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_tasks_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_tasks_assignee
        FOREIGN KEY (assignee_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_tasks_status
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'BLOCKED', 'DONE')),

    CONSTRAINT chk_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);



CREATE INDEX idx_team_memberships_user_id
    ON team_memberships(user_id);


CREATE INDEX idx_project_memberships_user_id
    ON project_memberships(user_id);

CREATE INDEX idx_tasks_project_id
    ON tasks(project_id);

CREATE INDEX idx_tasks_assignee_id
    ON tasks(assignee_id);

CREATE UNIQUE INDEX uq_workspace_one_owner
    ON workspace_memberships(workspace_id)
    WHERE role = 'OWNER';