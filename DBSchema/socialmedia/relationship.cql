create table followers_by_user_id(user_id text primary key , friend_id text, avatar text, bi_direction bit );
create table followers_by_idol_id(user_id text, idol_id text primary key , avatar text, bi_direction bit);
create table group_by_id (group_id text primary key, group_name text, group_description text, create_datetime timestamp, config map<text,int>, avatar text);
create table group_by_user(user_id text primary key, group_id text, group_name text , group_description text, config map<text,text>, avatar text, personal_config map<text, text>);
