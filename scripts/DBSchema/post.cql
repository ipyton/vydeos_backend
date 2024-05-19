CREATE KEYSPACE posts
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use posts;

create table posts_by_user_id(author_id text , post_id text,  author_name text, last_modified timestamp, objects list<text>, content text, access_type text, users set<text>, primary key ( author_id, post_id ));

create table posts_by_post_id(post_id text primary key, author_id text, author_name text, last_modified timestamp, objects list<text>, content text, access_type text, users set<text>);

create table mail_box(receiver_id text, send_time timestamp, sender_id text, pics list<text>, post_id text, notice list<text>, who_can_see list<text>, location text, primary key ( receiver_id, send_time ));