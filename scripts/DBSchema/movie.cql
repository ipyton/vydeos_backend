CREATE KEYSPACE movie
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use movie;


create table resource(resource_id text,  type text, resource text, name text,gid text,status text, primary key (resource_id,type, resource));

create table meta(resource_id text, poster text, score text, introduction text, movie_name text, tags text,
actor_list list<text>, release_year text, level text, picture_list list<text>, maker_list map<text,text>,
                  genre_list list<text>,type text, language text, primary key ( (resource_id, type,language) ));

create table movieGallery(resource_id text, type text, user_id text, poster text, score text, introduction text, movie_name text, tags text,
                          actor_list list<text>, release_year text, level text, picture_list list<text>, maker_list map<text,text>,
                          position int, genre_list list<text>, language text,  primary key ( (user_id), resource_id,type, language));

create table requests(resource_id text, movie_name text,type text, language text, create_time timestamp, userId text, release_year text,actor_list list<text>,  primary key ( resource_id,type,language ));
