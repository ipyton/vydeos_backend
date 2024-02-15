CREATE KEYSPACE userInfo
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use userInfo;
create table user_information(user_id text primary key, user_name text, avatar text, age date, intro text, apps list<text>);
create table settings_by_user(user_id text primary key, applicationId text primary key, config map<text, text>);
create table global_settings(setting_id text primary key, setting_name text, setting_value text);
