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


CREATE TABLE IF NOT EXISTS file_upload_status (user_email TEXT, resource_name TEXT, resource_id text, resource_type TEXT,
                                               format text,whole_hash TEXT, file_name text, size bigint,
                                               quality smallint, total_slices INT, current_slice INT, status_code INT, -- 上传状态：0-未开始, 1-正在上传, 2-正在处理, 3-已完成
                                               PRIMARY KEY ((resource_id,resource_type)) -- quality
);
-- 1.360p	640 × 360	16:9	低清，早期在线视频
-- 2.480p	720 × 480	4:3 / 16:9	DVD 标准，早期电视
-- 3.720p	1280 × 720	16:9	基础高清，流媒体常见
-- 4.1080p (FHD)	1920 × 1080	16:9	全高清，主流电视、流媒体
-- 5.1440p (2K, QHD)	2560 × 1440	16:9	高端手机、显示器
-- 6.4K (UHD)	3840 × 2160	16:9	4K 超高清，主流高端电视、电影
-- 7.8K (UHD-2)	7680 × 4320	16:9	未来高端电视、实验应用
