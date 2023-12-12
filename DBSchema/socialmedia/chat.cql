create table chat_record_by_id(message_id int primary key, group_id text, user_id text ,message text, send_time timestamp, object_id text);
create table chat_record_by_group(message_id int, group_id text primary key , user_id text ,message text, send_time timestamp, object_id text);
create table chat_record_by_user(message_id int, group_id text, user_id text primary key, message text, send_time timestamp, object_id text);

create table group_by_id (group_id text primary key, user_id text, group_name text, group_description text, create_datetime timestamp, config map<text,int>, avatar text);
create table group_by_user(user_id text primary key, group_id text, group_name text, group_description text, config map<text,text>, avatar text, personal_config map<text, text>);
