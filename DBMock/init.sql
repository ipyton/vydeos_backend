CREATE KEYSPACE user
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use user;
create table token (user_email varchar(20) primary key , user_name varchar(8), expire_datetime datetime);
create table user_info (user_email varchar(20) primary key , user_name varchar(8), password varchar(20),
                        introduction varchar(50), avatar varchar(50), date_of_birth date);


insert into user_info values('9999999', 'zsasda','123456', 'hello', 'sbdhjfb', date('2003-12-31 01:02:03'));
select * from user_info;