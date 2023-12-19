create table chat_record_by_id(group_id text primary key, message_id int primary key, user_id text ,message text, refer text, send_time timestamp, object_id list<text>, recall bit);

create table chat_group_details(groupId text primary key, group_name text, group_description text, config map<text,text>, avatar text, personal_config map<text, text>)
create table chat_group_by_id (group_id text primary key, user_id text primary key, user_name text);

//add group information here
create table chat_group_by_user(user_id text primary key, group_id text primary key, group_name text);
create table chat_group_invitation(group_id text primary keep, group_name text  , expire_time timestamp, user_id text, user_name text, limit int);