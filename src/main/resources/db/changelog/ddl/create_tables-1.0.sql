--liquibase formatted sql

--changeset aidar:1
CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(64) NOT NULL UNIQUE,
    first_name VARCHAR(64),
    last_name  VARCHAR(64),
    created_dt TIMESTAMP DEFAULT now()
);

--changeset aidar:2
CREATE TABLE IF NOT EXISTS chats
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(64),
    description VARCHAR(255),
    created_dt  TIMESTAMP DEFAULT now()
);

--changeset aidar:3
CREATE TABLE IF NOT EXISTS meetings
(
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT NOT NULL,
    address    VARCHAR(255),
    group_id   BIGINT,
    created_dt TIMESTAMP DEFAULT now(),
    state      VARCHAR(100),
    FOREIGN KEY (group_id) REFERENCES chats (id)
);

--changeset aidar:4
CREATE TABLE IF NOT EXISTS user_meetings
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    meeting_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE
);

--changeset aidar:5
CREATE TABLE IF NOT EXISTS subject
(
    id         SERIAL PRIMARY KEY,
    title      VARCHAR(100),
    duration   INT,
    meeting_id BIGINT NOT NULL,
    FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE
);
--changeset aidar:6
CREATE TABLE IF NOT EXISTS question
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(150),
    subject_id BIGINT NOT NULL,
    FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE
);
--changeset aidar:7
CREATE TABLE IF NOT EXISTS user_chat
(
    user_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE
);
--changeset aidar:8
CREATE TABLE IF NOT EXISTS user_settings
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    time_zone VARCHAR(10),
    language  VARCHAR(5),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
--changeset aidar:9
CREATE TABLE IF NOT EXISTS meeting_date
(
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    date       DATE,
    FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE
);
--changeset aidar:10
CREATE TABLE IF NOT EXISTS meeting_time
(
    id      BIGSERIAL PRIMARY KEY,
    time    TIMESTAMP NOT NULL,
    date_id BIGINT    NOT NULL,
    status  VARCHAR(10),
    FOREIGN KEY (date_id) REFERENCES meeting_date (id) ON DELETE CASCADE
);
--changeset aidar:11
CREATE TABLE IF NOT EXISTS user_times
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    meeting_time_id BIGINT NOT NULL,
    status          VARCHAR(10),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (meeting_time_id) REFERENCES meeting_time (id) ON DELETE CASCADE
);
--changeset aidar:12
CREATE TABLE IF NOT EXISTS bot_state
(
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT NOT NULL,
    msg_type VARCHAR(20),
    msg_id   INT,
    state    VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
