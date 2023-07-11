--liquibase formatted sql

--changeset aidar:create_users_table
CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(64),
    first_name VARCHAR(64) NOT NULL,
    last_name  VARCHAR(64),
    created_dt TIMESTAMP DEFAULT now()
);
--rollback drop table users;

--changeset aidar:create_chats_table
CREATE TABLE IF NOT EXISTS chat
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(64),
    description VARCHAR(255),
    created_dt  TIMESTAMP DEFAULT now()
);
--rollback drop table chat;

--changeset aidar:create_meetings_table
CREATE TABLE IF NOT EXISTS meeting
(
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT NOT NULL,
    address    VARCHAR(255),
    group_id   BIGINT,
    created_dt TIMESTAMP DEFAULT now(),
    updated_dt TIMESTAMP,
    state      VARCHAR(100),
    FOREIGN KEY (group_id) REFERENCES chat (id),
    FOREIGN KEY (owner_id) REFERENCES users (id)
);
--rollback drop table meeting;

--changeset aidar:create_user_meetings_table
CREATE TABLE IF NOT EXISTS user_meetings
(
    user_id    BIGINT NOT NULL,
    meeting_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (meeting_id) REFERENCES meeting (id) ON DELETE CASCADE
);
--rollback drop table user_meetings;

--changeset aidar:create_subject_table
CREATE TABLE IF NOT EXISTS subject
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(100) NOT NULL,
    duration   INT,
    meeting_id BIGINT       NOT NULL,
    FOREIGN KEY (meeting_id) REFERENCES meeting (id) ON DELETE CASCADE
);
--rollback drop table subject;

--changeset aidar:create_questions_table
CREATE TABLE IF NOT EXISTS question
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(150) NOT NULL,
    subject_id BIGINT       NOT NULL,
    FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE
);
--rollback drop table question;

--changeset aidar:create_user_chats_table
CREATE TABLE IF NOT EXISTS user_chats
(
    user_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (chat_id) REFERENCES chat (id) ON DELETE CASCADE
);
--rollback drop table user_chats;

--changeset aidar:create_user_settings_table
CREATE TABLE IF NOT EXISTS user_settings
(
    user_id  BIGINT PRIMARY KEY,
    zone_id  VARCHAR(10) NOT NULL DEFAULT 'UTC+03:00',
    language VARCHAR(5)  NOT NULL DEFAULT 'ru-RU',
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
--rollback drop table user_settings;

--changeset aidar:create_meeting_date_table
CREATE TABLE IF NOT EXISTS meeting_date
(
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    date       DATE   NOT NULL,
    FOREIGN KEY (meeting_id) REFERENCES meeting (id) ON DELETE CASCADE
);
--rollback drop table meeting_date;

--changeset aidar:create_meeting_time_table
CREATE TABLE IF NOT EXISTS meeting_time
(
    id        BIGSERIAL PRIMARY KEY,
    date_time TIMESTAMP NOT NULL,
    date_id   BIGINT    NOT NULL,
    FOREIGN KEY (date_id) REFERENCES meeting_date (id) ON DELETE CASCADE
);
--rollback drop table meeting_time;

--changeset aidar:create_user_times_table
CREATE TABLE IF NOT EXISTS user_times
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    meeting_time_id BIGINT       NOT NULL,
    status          VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (meeting_time_id) REFERENCES meeting_time (id) ON DELETE CASCADE
);
--rollback drop table user_times;

--changeset aidar:create_bot_state_table
CREATE TABLE IF NOT EXISTS bot_state
(
    user_id      BIGINT PRIMARY KEY,
    msg_type     VARCHAR(30),
    msg_from_bot BOOLEAN,
    updated_dt   TIMESTAMP,
    msg_id       INT,
    state        VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
--rollback drop table bot_state;

