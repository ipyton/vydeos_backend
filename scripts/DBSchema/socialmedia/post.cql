CREATE KEYSPACE posts
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use posts;
create table posts_by_article_id(post_id text primary key, author_id text, author_name text, last_modified timestamp, pictures list<text>, content text, access_type text, users set<text>);
create table posts_by_user_id(author_id text , post_id text,  author_name text, last_modified timestamp, pictures list<text>, content text, access_type text, users set<text>, primary key ( author_id, post_id ));

