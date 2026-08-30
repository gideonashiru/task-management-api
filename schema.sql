--
-- PostgreSQL database dump
--

\restrict CX0R1MdHaQ7mRszP6rpzHPSUJ0VMRmgaeglapK7WvOCW0C1HSuqliANFFjWrsfh

-- Dumped from database version 18.6 (Debian 18.6-1.pgdg13+2)
-- Dumped by pg_dump version 18.6 (Debian 18.6-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: project_memberships; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.project_memberships (
    id uuid NOT NULL,
    joined_at timestamp(6) without time zone NOT NULL,
    project_role character varying(255) NOT NULL,
    member_id uuid NOT NULL,
    project_id uuid NOT NULL,
    CONSTRAINT project_memberships_project_role_check CHECK (((project_role)::text = ANY ((ARRAY['OWNER'::character varying, 'MEMBER'::character varying])::text[])))
);


ALTER TABLE public.project_memberships OWNER TO admin;

--
-- Name: projects; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.projects (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    project_description character varying(3000),
    project_name character varying(255) NOT NULL,
    owner_id uuid NOT NULL
);


ALTER TABLE public.projects OWNER TO admin;

--
-- Name: tasks; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.tasks (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    task_description character varying(3000),
    status character varying(255) NOT NULL,
    task_title character varying(255) NOT NULL,
    assignee_id uuid,
    project_id uuid NOT NULL,
    CONSTRAINT tasks_status_check CHECK (((status)::text = ANY ((ARRAY['TODO'::character varying, 'IN_PROGRESS'::character varying, 'DONE'::character varying])::text[])))
);


ALTER TABLE public.tasks OWNER TO admin;

--
-- Name: users; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    name character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.users OWNER TO admin;

--
-- Name: project_memberships project_memberships_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.project_memberships
    ADD CONSTRAINT project_memberships_pkey PRIMARY KEY (id);


--
-- Name: projects projects_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (id);


--
-- Name: tasks tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (id);


--
-- Name: projects uk_projects_owner_project_name; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT uk_projects_owner_project_name UNIQUE (owner_id, project_name);


--
-- Name: project_memberships ukk2co7njcbuo4x07v2gd08lyop; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.project_memberships
    ADD CONSTRAINT ukk2co7njcbuo4x07v2gd08lyop UNIQUE (project_id, member_id);


--
-- Name: users ukr43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: project_memberships fk2dn9wlxj6beafbx8org69i8po; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.project_memberships
    ADD CONSTRAINT fk2dn9wlxj6beafbx8org69i8po FOREIGN KEY (member_id) REFERENCES public.users(id);


--
-- Name: project_memberships fkd90uwkxsbfhownoqpuw443iqf; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.project_memberships
    ADD CONSTRAINT fkd90uwkxsbfhownoqpuw443iqf FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: tasks fkekr1dgiqktpyoip3qmp6lxsit; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT fkekr1dgiqktpyoip3qmp6lxsit FOREIGN KEY (assignee_id) REFERENCES public.users(id);


--
-- Name: projects fkmueqy6cpcwpfl8gnnag4idjt9; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT fkmueqy6cpcwpfl8gnnag4idjt9 FOREIGN KEY (owner_id) REFERENCES public.users(id);


--
-- Name: tasks fksfhn82y57i3k9uxww1s007acc; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT fksfhn82y57i3k9uxww1s007acc FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- PostgreSQL database dump complete
--

\unrestrict CX0R1MdHaQ7mRszP6rpzHPSUJ0VMRmgaeglapK7WvOCW0C1HSuqliANFFjWrsfh

