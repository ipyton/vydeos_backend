CREATE KEYSPACE movie
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use movie;


create table resource(movieId text,  resource text, gid text, primary key (movieId, resource));

create table meta(movieId text primary key, poster text, score text, introduction text, movie_name text, tags text,
actress_list list<text>, release_year text, level text, picture_list list<text>, maker_list map<text,text>, genre_list list<text>);
