CREATE KEYSPACE userInfo
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use userInfo;
create table user_information(user_id text primary key, user_name text, avatar text, birthdate date,gender boolean, intro text, location text , language text, country text,apps list<text>);


create table settings_by_user(user_id text, applicationId text, config map<text, text>, primary key ( (user_id, applicationId) ));
create table global_settings(setting_id text primary key, setting_name text, setting_value text);
create table black_list(user_id text, black_user_id text, black_user_name text, black_user_avatar text, primary key ( user_id, black_user_id ));
