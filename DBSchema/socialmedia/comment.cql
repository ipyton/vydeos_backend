create table comments_by_user_id(user_id text primary key, comment_id text primary key, refer_comment_id text, comment_datetime timestamp, content text, type text, user_name text ,user_id text, avatar text);
create table comments_by_comment(comment_refer text primary key, comment_id text primary key, comment_datetime timestamp, likes int, content text, type text, user_name text ,user_id text, avatar text);
create table comments_by_object_id(object_id text primary key, comment_id text primary key,  comment_datetime timestamp,likes int, content text, type text, user_name text ,user_id text, avatar text);
create table app_comments(applicationId text primary key, userId text primary key, comment text, rate float, picture list<text>);
