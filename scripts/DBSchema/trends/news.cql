CREATE KEYSPACE trends
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use trends;
create table hot_video(time timestamp primary key, top int primary key, videoId text);
create table hot_article(time timestamp primary key, top int primary key, articleId text);
create table hot_application(time timestamp primary key, top int primary key, applicationId text);
