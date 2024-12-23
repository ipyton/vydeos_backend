CREATE KEYSPACE userInfo
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use userInfo;

create table user_auth (userId text, password text, email text, telephone text,temp boolean,roleId int, primary key ( userId ));
create table user_tokens (user_token text primary key , userId text,  invalid_date timestamp);

CREATE TABLE IF NOT EXISTS roles (
                                     roleId int PRIMARY KEY,         -- 角色唯一标识符
                                     roleName TEXT,                   -- 角色名称
                                     allowedPaths LIST<TEXT>         -- 允许访问的路径数组
);