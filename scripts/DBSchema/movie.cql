CREATE KEYSPACE movie
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use movie;


create table download_status (movieId text primary key , segment int, total int);

create table resource(movieId text,  resource text, status text, primary key (movieId, resource));

create table meta(movieId text primary key, poster text, score text, introduction text, movie_name text, tags list<text>,
actress_list list<text>, release_year text, level text, picture_list list<text>, maker_list list<text>, genre_list list<text>)
