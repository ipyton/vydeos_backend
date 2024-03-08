CREATE KEYSPACE group_chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use group_chat;
create table group_chat_record_by_id(group_id text, message_id int, user_id text ,groupMessage text, refer text, send_time timestamp, object_id list<text>, recall boolean, primary key ((group_id, message_id)));
create table group_chat_record_by_message_id(message_id int, group_id text , user_id text ,groupMessage text, send_time timestamp, object_id text, primary key ( group_id ));



create table chat_group_details(group_id text primary key, group_name text, group_description text, owner text,config map<text,text>, avatar text)
create table chat_group_by_id (group_id text, user_id text , user_name text, primary key ((group_id, user_id)));


create table chat_group_by_user(user_id text, group_id text, group_name text, primary key (user_id, group_id));
create table chat_group_invitation (group_id text, group_name text, expire_time timestamp, user_id text, user_name text, limitation int, primary key (group_id));