CREATE KEYSPACE files
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use files;


// this include every one's own workspace directory, for users avatar information,it creates

// create table avatar_information(user_id text primary key, user_name text, avatar text, birthdate date,gender boolean, intro text, apps list<text>);

create table IF NOT EXISTS chat_pictures(user_id text,chat_id text, links list<text>, primary key ( user_id, chat_id ));

create table IF NOT EXISTS post_pictures(user_id text, post_id text, links list<text>,primary key ( user_id, post_id ));

create table IF NOT EXISTS assets(user_id text, bucket text, file text,primary key ( user_id ));


CREATE TABLE IF NOT EXISTS file_upload_status (user_email TEXT, resource_name TEXT, resource_id UUID, resource_type TEXT,
                                                  whole_hash TEXT, total_slices INT, current_slice INT, status_code INT,  -- 上传状态：0-未开始, 1-正在上传, 2-正在处理, 3-已完成
                                                  PRIMARY KEY ((resource_id,resource_type))
);

