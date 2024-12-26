CREATE KEYSPACE chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use chat;
//create table chat_record_by_id(user_id text, receiver_id text, message_id text, content text, send_time timestamp, type text, object_id text, primary key ( (user_id, receiver_id), send_time));

create table chat_records(user_id text, receiver_id text, message_id bigint, content text, send_time timestamp, type text, refer_message_id bigint, refer_user_id list<text>,  del boolean, primary key ((user_id), message_id));
CREATE INDEX ON chat.chat_records (receiver_id);


create index get_chat_by_user_receiver_id on chat_records ((user_id), receiver_id);
create index get_chat_by_user_id_time on chat_records ((user_id), send_time);