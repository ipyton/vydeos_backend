CREATE KEYSPACE movie
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use movie;


create table resource(resource_id text,  type text, resource text, name text,gid text,status text, quality int, season_id int,episode int, primary key ((resource_id,type,season_id, episode), resource,quality));

create table meta(resource_id text, poster text, score text, introduction text, movie_name text, tags text,
actor_list list<text>, release_year text, level text, picture_list list<text>, maker_list map<text,text>,
                  genre_list list<text>,type text, language text, total_season int, primary key ( (resource_id, type,language) ));

//tv 1
//movie 0

create table movieGallery(resource_id text, type text, user_id text, poster text, score text, introduction text, movie_name text, tags text,
                          actor_list list<text>, release_year text, level text, picture_list list<text>, maker_list map<text,text>,
                          position int, genre_list list<text>, language text,  primary key ( (user_id), resource_id,type, language));

create table requests(resource_id text, movie_name text,type text, language text, create_time timestamp, userId text, release_year text,actor_list list<text>,  primary key ( resource_id,type,language ));

create table playable(
                         resource_id text,
                         type text,
                         season_id int,
                         episode int,
                         quality tinyint,
                         bucket text,
                         path text,
                         PRIMARY KEY ((resource_id, type), season_id, episode, quality));
create table subtitles(resource_id text, type text, language text, bucket text,episode int, season_id int, path text, primary key ((resource_id, type,season_id, episode),language));
create table comments(resource_id text, type text, userId text, time time, content text, likes bigint, season_id int,episode int, primary key ( (resource_id, type, season_id, episode), time, userId ) );
create table season_meta(resource_id text, type text, total_episode int, season_id int, primary key ( (resource_id, type, season_id) ) );


-- 1.360p	640 × 360	16:9	低清，早期在线视频
-- 2.480p	720 × 480	4:3 / 16:9	DVD 标准，早期电视
-- 3.720p	1280 × 720	16:9	基础高清，流媒体常见
-- 4.1080p (FHD)	1920 × 1080	16:9	全高清，主流电视、流媒体
-- 5.1440p (2K, QHD)	2560 × 1440	16:9	高端手机、显示器
-- 6.4K (UHD)	3840 × 2160	16:9	4K 超高清，主流高端电视、电影
-- 7.8K (UHD-2)	7680 × 4320	16:9	未来高端电视、实验应用
