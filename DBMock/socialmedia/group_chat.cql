CREATE KEYSPACE group_chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use group_chat;
create table chat_record_by_id(group_id text primary key, message_id int primary key, user_id text ,groupMessage text, refer text, send_time timestamp, object_id list<text>, recall bit);

create table chat_group_details(group_id text primary key, group_name text, group_description text, owner text,config map<text,text>, avatar text, config map<text, text>);
create table chat_group_by_id (group_id text primary key, user_id text primary key, user_name text);

//add group information here
create table chat_group_by_user(user_id text primary key, group_id text primary key, group_name text);
create table chat_group_invitation(group_id text primary keep, group_name text  , expire_time timestamp, user_id text, user_name text, limit int);