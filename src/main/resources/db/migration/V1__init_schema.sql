CREATE TABLE users (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    name character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username)
);

CREATE TABLE projects (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    project_description character varying(3000),
    project_name character varying(255) NOT NULL,
    owner_id uuid NOT NULL,
    CONSTRAINT projects_pkey PRIMARY KEY (id),
    CONSTRAINT uk_projects_owner_project_name UNIQUE (owner_id, project_name),
    CONSTRAINT fkmueqy6cpcwpfl8gnnag4idjt9 FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE tasks (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    task_description character varying(3000),
    status character varying(255) NOT NULL,
    task_title character varying(255) NOT NULL,
    assignee_id uuid,
    project_id uuid NOT NULL,
    CONSTRAINT tasks_pkey PRIMARY KEY (id),
    CONSTRAINT tasks_status_check CHECK (((status)::text = ANY ((ARRAY['TODO'::character varying, 'IN_PROGRESS'::character varying, 'DONE'::character varying])::text[]))),
    CONSTRAINT fkekr1dgiqktpyoip3qmp6lxsit FOREIGN KEY (assignee_id) REFERENCES users(id),
    CONSTRAINT fksfhn82y57i3k9uxww1s007acc FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE project_memberships (
    id uuid NOT NULL,
    joined_at timestamp(6) without time zone NOT NULL,
    project_role character varying(255) NOT NULL,
    member_id uuid NOT NULL,
    project_id uuid NOT NULL,
    CONSTRAINT project_memberships_pkey PRIMARY KEY (id),
    CONSTRAINT project_memberships_project_role_check CHECK (((project_role)::text = ANY ((ARRAY['OWNER'::character varying, 'MEMBER'::character varying])::text[]))),
    CONSTRAINT ukk2co7njcbuo4x07v2gd08lyop UNIQUE (project_id, member_id),
    CONSTRAINT fk2dn9wlxj6beafbx8org69i8po FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT fkd90uwkxsbfhownoqpuw443iqf FOREIGN KEY (project_id) REFERENCES projects(id)
);