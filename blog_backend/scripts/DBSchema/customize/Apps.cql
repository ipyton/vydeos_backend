CREATE KEYSPACE apps
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use apps;

create table application(applicationId text, name text, version text, author text ,
introduction text, lastModified timestamp, ratings float, type text, system_requirements map<text, text>,
history_versions list<text>, pictures list<text>, hot_comments list<text>, primary key(applicationId,version));


//create table simple_application_intro(applicationId text , name text, introduction text, pictures list<text>
//    ,primary key(applicationId));


create table application_config_template (applicationId text , version text, config_template map<text, text>, primary key((applicationId,version)));

create table user_apps (userId text, applicationId text, name text, introduction text, pictures list<text>, primary key ( userId, applicationId ));

create table app_comment ( appId text, commentId text, userId text, userName text, userAvatar text, comment text, rate float, commentDateTime timestamp, primary key ((appId, commentId),userId));