CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(2000),
    ctime TIMESTAMP WITH TIME ZONE NOT NULL,
    utime TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE project_members (
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_account_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, user_account_id)
);

CREATE INDEX project_members_user_account_id_idx ON project_members(user_account_id);
