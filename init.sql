use blog;
create table token (user_id char(8) primary key , user_name varchar(8), expire_datetime datetime);
create table user_info (user_id char(8) primary key , user_name varchar(8), password varchar(20),
                        introduction varchar(50), avatar varchar(50), date_of_birth datetime);


