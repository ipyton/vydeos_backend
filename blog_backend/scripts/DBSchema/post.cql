CREATE KEYSPACE posts
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use posts;

create table posts_by_user_id( post_id text, likes int,author_id text ,  author_name text,  comments list<text>, last_modified timestamp, images list<text>, videos list<text>, voices list<text> , content text, access_rules list<text>, notice list<text>, location text, primary key ( author_id, last_modified )) WITH CLUSTERING ORDER BY (last_modified DESC);

create table posts_by_post_id(post_id text primary key, likes int, author_id text, author_name text, comments list<text>, last_modified timestamp, images list<text>, videos list<text>, voices list<text> , content text, access_rules list<text>, notice list<text>, location text);

create table mail_box(receiver_id text, last_modified timestamp, likes int, comments list<text>,content text, author_id text,author_name text,  images list<text>, videos list<text>, voices list<text> , post_id text, notice list<text>, access_rules list<text>, location text, primary key ( (receiver_id), last_modified )) WITH CLUSTERING ORDER BY (last_modified DESC);