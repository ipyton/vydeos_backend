
use blog;
create table group_by_id (group_id text primary key, group_name text, group_description text, create_datetime timestamp, config map<text,int>, avatar text);
create table group_by_user(user_id text primary key, group_id text, group_name text , group_description text, config map<text,text>, avatar text, personal_config map<text, text>);

create table chat_record_by_id(message_id int primary key, group_id text, user_id text ,message text, send_time timestamp, object_id text);
create table chat_record_by_group(message_id int, group_id text primary key , user_id text ,message text, send_time timestamp, object_id text);
create table chat_record_by_user(message_id int, group_id text, user_id text primary key, message text, send_time timestamp, object_id text);

create table user_information(user_id text primary key, user_name text, avatar text, age date, intro text);

create table settings_by_user(user_id text primary key, setting_id text, setting_value text);
create table global_settings(setting_id text primary key, setting_name text, setting_value text);

create table followers_by_user_id(user_id text primary key , friend_id text, avatar text);
create table followers_by_idol_id(user_id text, idol_id text primary key , avatar text);

create table comments_by_user_id(user_id text primary key, comment_id text, date timestamp, content text, type text, attributes map<text, text>);
create table comments_by_comment(comment_refer text primary key, comment_id text, date timestamp, content text, type text, attributes map<text, text>);

create table articles_by_article_id(article_id text primary key, author_id text, author_name text, last_modified timestamp, pictures list<text>, content text);
create table articles_by_user_id(article_id text, author_id text primary key , author_name text, last_modified timestamp, pictures list<text>, content text);




