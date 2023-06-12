--liquibase formatted sql

--changeset aidar:1
CREATE TABLE IF NOT EXISTS users
(
    id            BIGINT PRIMARY KEY,
    username      VARCHAR(64) NOT NULL UNIQUE,
    first_name    VARCHAR(64),
    last_name     VARCHAR(64),
    language_code VARCHAR(5),
    created_dt    DATE
);

--changeset aidar:2
CREATE TABLE IF NOT EXISTS chats
(
    id          BIGINT PRIMARY KEY,
    title       VARCHAR(64),
    biography   VARCHAR(255),
    first_name  VARCHAR(100),
    last_name   VARCHAR(100),
    description VARCHAR(255),
    started_dt  DATE
);

--changeset aidar:3
CREATE TABLE IF NOT EXISTS meetings
(
    id         BIGINT PRIMARY KEY,
    owner_id   BIGINT NOT NULL,
    address    VARCHAR(255),
    group_id   BIGINT,
    created_dt DATE,
    state      VARCHAR(100),
    FOREIGN KEY (group_id) REFERENCES chats (id)
);

--changeset aidar:4
CREATE TABLE IF NOT EXISTS user_meetings
(
    user_id    BIGINT NOT NULL,
    meeting_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (meeting_id) REFERENCES meetings (id)
);

--changeset aidar:5
CREATE TABLE IF NOT EXISTS subject
(
    id         BIGINT PRIMARY KEY,
    title      VARCHAR(100),
    meeting_id BIGINT NOT NULL,
    FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE
);
--changeset aidar:6
CREATE TABLE IF NOT EXISTS question
(
    id         BIGINT PRIMARY KEY,
    title      VARCHAR(150),
    created_dt DATE,
    subject_id BIGINT NOT NULL,
    FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE
);
--changeset aidar:7
CREATE TABLE IF NOT EXISTS user_chat
(
    user_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (chat_id) REFERENCES chats (id)
);
--changeset aidar:8
CREATE TABLE IF NOT EXISTS user_settings
(
    id        SERIAL PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    time_zone TIMESTAMP,
    lang      VARCHAR(5),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
