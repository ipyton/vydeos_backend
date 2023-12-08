create table chat_record_by_id(message_id int primary key, group_id text, user_id text ,message text, send_time timestamp, object_id text);
create table chat_record_by_group(message_id int, group_id text primary key , user_id text ,message text, send_time timestamp, object_id text);
create table chat_record_by_user(message_id int, group_id text, user_id text primary key, message text, send_time timestamp, object_id text);

