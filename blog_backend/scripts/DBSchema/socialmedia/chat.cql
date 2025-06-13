CREATE KEYSPACE chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use chat;
//create table chat_record_by_id(user_id text, receiver_id text, message_id text, content text, send_time timestamp, type text, object_id text, primary key ( (user_id, receiver_id), send_time));

drop table chat.chat_records;
drop table chat.group_chat_records;


// 发送方+ 接收方+ id范围查询
//direction user1->user2 true
create table chat.chat_records(user_id1 text, user_id2 text, direction boolean,relationship boolean,group_id bigint, message_id bigint, content text,messagetype text, send_time timestamp, refer_message_id bigint, refer_user_id list<text>,  del boolean,session_message_id bigint, PRIMARY KEY ((user_id1, user_id2), session_message_id));

create table chat.group_chat_records(user_id text,group_id bigint, message_id bigint, content text, messagetype text, send_time timestamp, type text, refer_message_id bigint, refer_user_id list<text>,  del boolean, session_message_id bigint, primary key ((group_id), session_message_id));



create index get_chat_by_user_receiver_id on chat_records ((user_id), receiver_id, type);
create index get_chat_by_user_id_time on chat_records ((user_id), send_time);

CREATE TABLE web_push_endpoints (
                                             user_id text,                  -- 用户 ID，分区键的一部分
                                             endpoint TEXT,                 -- 每个用户的订阅 Endpoint
                                             expiration_time TIMESTAMP,     -- 到期时间
                                             p256dh TEXT,                   -- Web Push 密钥
                                             auth TEXT,                     -- Web Push auth
                                             PRIMARY KEY (user_id, endpoint) -- 复合主键，确保用户的每个 endpoint 唯一
);
drop table chat.unread_messages;
create table chat.unread_messages(user_id text, sender_id text,member_id text,group_id bigint, type text, messageType text, content text, send_time timestamp, message_id bigint, session_message_id bigint, count int, primary key (user_id,type,group_id,sender_id)  );
