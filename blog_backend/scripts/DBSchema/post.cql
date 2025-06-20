CREATE KEYSPACE posts
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use posts;

drop table posts.posts_by_post_id;
drop table posts.posts_by_user_id;
drop table posts.temporary_post_pics;

create table posts.temporary_post_pics(author_id text primary key, path text, timestamp timestamp );

create table posts.posts_by_user_id( post_id bigint, likes int,author_id text ,  author_name text,  comments list<text>, last_modified timestamp, images list<text>, videos list<text>, voices list<text> , content text, access_rules list<text>, notice list<text>, location text, primary key ( author_id, last_modified )) WITH CLUSTERING ORDER BY (last_modified DESC);

create table posts.posts_by_post_id(post_id bigint primary key, likes int, author_id text, author_name text, comments list<text>, last_modified timestamp, images list<text>, videos list<text>, voices list<text> , content text, access_rules list<text>, notice list<text>, location text);

drop table posts.mail_box;

create table posts.mail_box(receiver_id text, last_modified timestamp, likes int, comments list<text>,content text, author_id text,author_name text,  images list<text>, videos list<text>, voices list<text> , post_id bigint, notice list<text>, access_rules list<text>, location text, primary key ( (receiver_id), last_modified )) WITH CLUSTERING ORDER BY (last_modified DESC);