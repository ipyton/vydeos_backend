CREATE KEYSPACE apps
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use apps;

create table application(applicationId text primary key, name text, version text primary key, author text,introduction text, lastModified timestamp, ratings float, type text, system_requirements map<text, text>, history_versions list<text>, pictures list<text>, hot_comments list<text>);
create table simple_application_intro(applicationId text primary key, name text, introduction text, pictures list<text>);
create table application_config_template(applicationId text primary key , version text primary key , config_template map<text, text>);
create table user_apps(userId text primary key, applicationId text primary key, name text, introduction text, pictures list<text>);
