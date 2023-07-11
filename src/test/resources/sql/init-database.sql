INSERT INTO users (id, username, first_name, last_name)
VALUES (1, 'andrey@gmail.com', 'Andrey', 'Makarov');

INSERT INTO bot_state (user_id, msg_type, msg_from_bot, msg_id, state)
VALUES (1, 'SEND_MESSAGE', true, 5213566, 'CREATE');

INSERT INTO user_settings (user_id, zone_id, language)
VALUES (1, 'UTC+03:00', 'ru-RU');

INSERT INTO chat (id, title, description)
VALUES (1, 'Kafka', 'Chat for funs MQ');

INSERT INTO meeting (id, owner_id, address, group_id, state)
VALUES (1, 1, '403 room', 1, 'GROUP_SELECT');

INSERT INTO meeting_date(id, meeting_id, date)
VALUES (1, 1, now());

INSERT INTO meeting_time(id, date_id, date_time)
VALUES (1, 1, now());

INSERT INTO user_times(id, user_id, meeting_time_id, status)
VALUES (1, 1, 1, 'CONFIRMED')
