-- ============================================================
-- V2 — Seed demo data
--
-- Creates three users (demo, alice, bob), one showcase project
-- owned by demo, both alice and bob added as members, and three
-- tasks covering every status in the task lifecycle:
--   TODO  →  IN_PROGRESS  →  DONE
--
-- Fixed UUIDs ensure this migration is deterministic across
-- all environments (local Docker, Aiven, Render).
-- ============================================================

-- ----------------------------------------------------------
-- Users
-- ----------------------------------------------------------
INSERT INTO users (id, created_at, username, password_hash, name)
VALUES
    -- password: demo1234
    ('00000000-0000-0000-0000-000000000001',
     '2026-01-01 00:00:00',
     'demo',
     '$2a$10$TZLfb5c.2O78XcGSjhlQo.edE27F5RyvhippPg1zMwPeZDSA756bK',
     'Demo User'),

    -- password: alice1234
    ('00000000-0000-0000-0000-000000000002',
     '2026-01-01 00:00:00',
     'alice',
     '$2a$10$GJao3poYI6iZ7fIyrevYqeJCiZ4mAQwJsiH5XYPYIHSIptC1WykIa',
     'Alice'),

    -- password: bob1234
    ('00000000-0000-0000-0000-000000000003',
     '2026-01-01 00:00:00',
     'bob',
     '$2a$10$.0c54lEOtOOMbNpRWRR0tOOo9PJFFTggmu7ecysi8wBJESItElpPW',
     'Bob')
ON CONFLICT (username) DO NOTHING;

-- ----------------------------------------------------------
-- Project  (owned by demo)
-- ----------------------------------------------------------
INSERT INTO projects (id, created_at, project_name, project_description, owner_id)
VALUES
    ('00000000-0000-0000-0000-000000000010',
     '2026-01-01 00:00:00',
     'Showcase Project',
     'A live demo project showing the full task lifecycle. ' ||
     'Try logging in as demo / alice / bob to explore the API.',
     '00000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------
-- Project memberships
-- ----------------------------------------------------------
INSERT INTO project_memberships (id, joined_at, project_role, member_id, project_id)
VALUES
    -- demo is OWNER
    ('00000000-0000-0000-0000-000000000030',
     '2026-01-01 00:00:00',
     'OWNER',
     '00000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000000010'),

    -- alice is MEMBER
    ('00000000-0000-0000-0000-000000000031',
     '2026-01-01 00:01:00',
     'MEMBER',
     '00000000-0000-0000-0000-000000000002',
     '00000000-0000-0000-0000-000000000010'),

    -- bob is MEMBER
    ('00000000-0000-0000-0000-000000000032',
     '2026-01-01 00:02:00',
     'MEMBER',
     '00000000-0000-0000-0000-000000000003',
     '00000000-0000-0000-0000-000000000010')
ON CONFLICT DO NOTHING;

-- ----------------------------------------------------------
-- Tasks  (one in each status)
-- ----------------------------------------------------------
INSERT INTO tasks (id, created_at, task_title, task_description, status, assignee_id, project_id)
VALUES
    -- TODO: unassigned, waiting to be picked up
    ('00000000-0000-0000-0000-000000000020',
     '2026-01-02 09:00:00',
     'Design the API endpoints',
     'Define the REST resource model, URL structure, request/response shapes, and error codes.',
     'TODO',
     NULL,
     '00000000-0000-0000-0000-000000000010'),

    -- IN_PROGRESS: assigned to alice
    ('00000000-0000-0000-0000-000000000021',
     '2026-01-02 09:30:00',
     'Implement JWT authentication',
     'Add stateless JWT-based auth: register, login, and a per-request filter that validates the Bearer token.',
     'IN_PROGRESS',
     '00000000-0000-0000-0000-000000000002',
     '00000000-0000-0000-0000-000000000010'),

    -- DONE: completed by bob
    ('00000000-0000-0000-0000-000000000022',
     '2026-01-02 10:00:00',
     'Write unit tests for service layer',
     'Cover UserService, ProjectService, and TaskService with JUnit 5 + Mockito. Target all happy paths and key error cases.',
     'DONE',
     '00000000-0000-0000-0000-000000000003',
     '00000000-0000-0000-0000-000000000010')
ON CONFLICT DO NOTHING;
