CREATE KEYSPACE chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use chat;
create table chat_record_by_id(user_id text, receiver_id text, message_id text, content text, send_time timestamp, object_id text, primary key ( (user_id, receiver_id), message_id));

