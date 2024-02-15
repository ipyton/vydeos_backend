CREATE KEYSPACE trends
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use trends;
create table hot_video(time timestamp, top int, videoId text, primary key ((time, top)));
create table hot_article(time timestamp, top int, articleId text, primary key ( time, top ));
create table hot_application(time timestamp , top int, applicationId text, primary key ( time, top ));
