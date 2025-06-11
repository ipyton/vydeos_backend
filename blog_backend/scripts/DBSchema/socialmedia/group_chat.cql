CREATE KEYSPACE group_chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use group_chat;
//create table group_chat_record_by_message_id(message_id int, group_id text , user_id text ,groupMessage text, send_time timestamp, object_id text, primary key ( group_id ));



create table group_chat.chat_group_details (group_id bigint primary key, name text, introduction text, owner_id text,config map<text,text>, avatar text, create_time timestamp,allow_invite_by_token boolean);
drop table group_chat.chat_group_members_by_group;
drop table group_chat.chat_group_members_by_user;
create table group_chat.chat_group_members_by_user (group_id bigint, user_id text, group_name text,primary key (user_id, group_id));
create table group_chat.chat_group_members_by_group (group_id bigint, user_id text , user_name text, primary key (group_id, user_id));

create table group_chat.chat_messages_mailbox (user_id text , group_id bigint, type text, content text, count text, latest_timestamp timestamp, primary key ( user_id,group_id ));
create table group_chat.invitations (source_id bigint, source_type text, expire_time timestamp, target_id text, target_type text, limitation int, primary key (source_id, source_type, target_id, target_type));