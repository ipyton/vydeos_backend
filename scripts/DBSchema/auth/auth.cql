CREATE KEYSPACE userInfo
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use userInfo;

create table user_auth (userId text, password text, email text, telephone text,temp boolean,roleId int, primary key ( userId ));
create table user_tokens (user_token text primary key , userId text,  invalid_date timestamp);
create table user_registration_code (userId text primary key, code text, expire_time timestamp);

CREATE TABLE IF NOT EXISTS roles (
                                     roleId int,         -- role identifier
                                     roleName TEXT,                   -- role name
                                     path text, name text, version text, icon_name text, primary key ( roleId, path, version )
);