CREATE KEYSPACE chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use chat;
create table chat_record_by_id(user_id text, receiver_id text, message_id text, group_id text ,groupMessage text, send_time timestamp, object_id text, primary key ( (user_id, receiver_id), message_id));
create table black_list(user_id text, black_id text, black_user_name text, black_user_avatar text, primary key ( user_id, black_id ));
create table chat_record_by_group(message_id int, group_id text , user_id text ,groupMessage text, send_time timestamp, object_id text, primary key ( group_id ));

