CREATE KEYSPACE posts
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use posts;

create table posts_by_user_id(author_id text , post_id text,  author_name text, last_modified timestamp, images list<text>, videos list<text>, voices list<text> , content text, access_rules list<text>, notice list<text>, location text, primary key ( author_id, post_id ));

create table posts_by_post_id(post_id text primary key, author_id text, author_name text, last_modified timestamp, images list<text>, videos list<text>, voices list<text> , content text, access_rules list<text>, notice list<text>, location text);

create table mail_box(receiver_id text, last_modified timestamp, content text, author_id text,author_name text,  images list<text>, videos list<text>, voices list<text> , post_id text, notice list<text>, access_rules list<text>, location text, primary key ( receiver_id, last_modified ));