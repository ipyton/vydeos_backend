CREATE KEYSPACE chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use chat;
create table chat_record_by_id(user_id text primary key, receiver_id text primary key, message_id text primary key ,group_id text ,groupMessage text, send_time timestamp, object_id text);
create table black_list(user_id text primary key, black_id text primary key, black_user_name text, black_user_avatar text);
create table chat_record_by_group(message_id int, group_id text primary key , user_id text ,groupMessage text, send_time timestamp, object_id text);

