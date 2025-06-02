CREATE KEYSPACE userInfo
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use userInfo;
create table user_information(user_id text primary key, user_name text, avatar text, birthdate date, gender boolean, intro text, location text , language text, country text, apps list<text>);


create table settings_by_user(user_id text, application_id text, key text, value text, primary key ( user_id, application_id, key ));

create table global_settings(application_id text, key text, value text, description text, primary key ( application_id, key ));

create table black_list(user_id text, black_user_id text, black_user_name text, black_user_avatar text, primary key ( user_id, black_user_id ));

create table settings_template(application_id text, key text, primary key ( application_id ));