CREATE KEYSPACE group_chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use group_chat;
create table group_chat_record_by_id(group_id bigint, message_id bigint,type text,  user_id bigint ,content text, referUserID bigint, referMessageId bigint, send_time timestamp, media text, recall boolean, primary key (group_id, message_id));
//create table group_chat_record_by_message_id(message_id int, group_id text , user_id text ,groupMessage text, send_time timestamp, object_id text, primary key ( group_id ));



create table chat_group_details (group_id bigint primary key, group_name text, group_description text, owner text,config map<text,text>, avatar text, create_time timestamp);
create table chat_group_members (group_id bigint, user_id bigint , user_name text, group_name text,primary key (group_id, user_id));

create table chat_messages_mailbox (user_id bigint , group_id bigint, type text, content text, count text, latest_timestamp timestamp, primary key ( user_id,group_id ));
create table chat_group_by_user(user_id bigint, group_id bigint, group_name text, primary key (user_id, group_id));
create table chat_group_invitation (group_id bigint, group_name text, expire_time timestamp, user_id text, user_name text, limitation int, primary key (group_id));