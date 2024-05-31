CREATE KEYSPACE files
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use files;


// this include every one's own workspace directory, for users avatar information,it creates

// create table avatar_information(user_id text primary key, user_name text, avatar text, birthdate date,gender boolean, intro text, apps list<text>);

create table chat_pictures(user_id text,chat_id text, links list<text>, primary key ( user_id, chat_id ));

create table post_pictures(user_id text, post_id text, links list<text>,primary key ( user_id, post_id ));

create table assets(user_id text, bucket text, file text,primary key ( user_id ))