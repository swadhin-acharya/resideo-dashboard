CREATE TABLE organizations (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    settings    TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE projects (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(100) NOT NULL,
    description     TEXT,
    settings        TEXT,
    archived        BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(organization_id, slug)
);

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    display_name    VARCHAR(255),
    avatar_url      TEXT,
    global_role     VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled         BOOLEAN DEFAULT TRUE,
    last_login_at   TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE project_memberships (
    id          UUID PRIMARY KEY,
    project_id  UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role        VARCHAR(20) NOT NULL DEFAULT 'AUTOMATION_ENGINEER',
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(project_id, user_id)
);

CREATE TABLE api_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id  UUID REFERENCES projects(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    token_prefix VARCHAR(10) NOT NULL,
    last_used_at TIMESTAMP WITH TIME ZONE,
    expires_at  TIMESTAMP WITH TIME ZONE,
    enabled     BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

ALTER TABLE executions ADD COLUMN project_id UUID REFERENCES projects(id);
ALTER TABLE executions ADD COLUMN user_id UUID REFERENCES users(id);
ALTER TABLE executions ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PROJECT';
ALTER TABLE executions ADD COLUMN machine_name VARCHAR(255);
ALTER TABLE executions ADD COLUMN source VARCHAR(20) DEFAULT 'DASHBOARD';

ALTER TABLE devices ADD COLUMN project_id UUID REFERENCES projects(id);
ALTER TABLE thermostats ADD COLUMN project_id UUID REFERENCES projects(id);
ALTER TABLE schedules ADD COLUMN project_id UUID REFERENCES projects(id);
ALTER TABLE schedules ADD COLUMN user_id UUID REFERENCES users(id);

CREATE INDEX idx_executions_project_id ON executions(project_id);
CREATE INDEX idx_executions_user_id ON executions(user_id);
CREATE INDEX idx_executions_visibility ON executions(visibility);
CREATE INDEX idx_devices_project_id ON devices(project_id);
CREATE INDEX idx_thermostats_project_id ON thermostats(project_id);
CREATE INDEX idx_schedules_project_id ON schedules(project_id);
CREATE INDEX idx_project_memberships_project ON project_memberships(project_id);
CREATE INDEX idx_project_memberships_user ON project_memberships(user_id);
CREATE INDEX idx_api_tokens_user ON api_tokens(user_id);
CREATE INDEX idx_api_tokens_hash ON api_tokens(token_hash);
