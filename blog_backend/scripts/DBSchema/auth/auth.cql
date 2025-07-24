CREATE KEYSPACE userInfo
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use userInfo;

create table user_auth (userId text, password text, email text, telephone text,temp boolean,roleId int, primary key ( userId ));
create table user_tokens (user_token text primary key , userId text,  invalid_date timestamp);
CREATE INDEX ON user_tokens (userId);


CREATE TABLE IF NOT EXISTS role_auths (
                                     roleId int,
                                     roleName TEXT,
                                     path text, name text, version int, type text, primary key ( roleId, type,path, version )
);

create table if not exists roles (
    roleId int primary key, roleName text,
);

insert into roles (roleId,roleName) values(0,'admin');
insert into roles (roleId,roleName) values(1, 'new_user');
use userinfo;
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/chat', 'Chat', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/trending', 'Trending', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/', 'Posts', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/videolist', 'Videos', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/about', 'About', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/logs', 'Version Log', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/qa', 'Ask', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/settings', 'Settings', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/friends', 'Friends', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/downloadRequestManager', 'Handle Download Request', 0, 'nav' );
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/userManage', 'User Management', 0, 'nav' );
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/role', 'Role Management', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/download', 'Download', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/post', 'Download', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/**', 'All', 0, 'api' );
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/edit', 'Edit', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/chat', 'Chat', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/trending', 'Trending', 0, 'nav' );
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/', 'Posts', 0, 'nav' );
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/videolist', 'Videos', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/about', 'About', 0, 'nav' );

insert into role_auths (roleId, path, name, version, type) VALUES (0, '/logs', 'Version Log', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/qa', 'Ask', 0, 'nav' );
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/settings', 'Settings', 0,'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (0, '/friends', 'Friends', 0, 'nav');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/i18n/**', 'languages', 0, 'api');


insert into role_auths (roleId, path, name, version, type) VALUES (1, '/account/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/auth/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/article/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/search/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/api/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/movie/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/friends/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/chat/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/file/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/post/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/gallery/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/comment/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/play_videos/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/movie_management/\*\*', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/movie/get_meta', 'Friends', 0, 'api');

insert into role_auths (roleId, path, name, version, type) VALUES (1, '/account/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/auth/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/article/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/search/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/api/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/movie/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/friends/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/chat/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/file/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/post/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/gallery/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/comment/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/play_videos/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/movie_management/**', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/movie/get_meta', 'Friends', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (0,  '/**', 'All Permissions ', 0, 'api');

use userinfo;
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/auth/getNavPaths', 'Get Auths', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/chat/**', 'All chats related things', 0, 'api');
insert into role_auths (roleId, path, name, version, type) VALUES (1, '/gallery/**', 'All friends Related things', 0, 'api');

insert into userinfo.role_auths (roleId, path, name, version, type) VALUES (1, '/gallery/**', 'All friends Related things', 0, 'api');

CREATE TABLE if not exists userinfo.user_registration_code  (   kind text,
                                                 userid text,
                                                 code text,
                                                 expire_time timestamp,
                                                 PRIMARY KEY (kind, userid)
);

update user_auth set roleId = 0 where email = 'czh1278341834@gmail.com';