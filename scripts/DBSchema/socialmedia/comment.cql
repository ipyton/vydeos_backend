CREATE KEYSPACE comment
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use comment;
create table comments_by_user_id(user_id text , comment_id text , refer_comment_id text, comment_datetime timestamp, content text, type text, user_name text, avatar text, primary key ( (user_id, comment_id) ));
create table comments_by_content(comment_refer text , comment_id text , refer text, comment_datetime timestamp, likes int, content text, type text, user_name text ,user_id text, avatar text, primary key ( (comment_refer, comment_id) ));
// create table comments_by_object_id(object_id text primary key, comment_id text primary key,  comment_datetime timestamp,likes int, content text, type text, user_name text ,user_id text, avatar text);
create table app_comments(applicationId text, userId text , comment text, rate float, picture list<text>, primary key ( (applicationId,userId) ));

create table comments(resource_id text, type text, user_id text, time timestamp, content text, likes bigint, primary key ( (resource_id, type),time, user_id ) )
CREATE INDEX ON comments(userId);
