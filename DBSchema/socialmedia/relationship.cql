create table followers_by_user_id(user_id text primary key , friend_id text, avatar text, bi_direction bit);
create table followers_by_idol_id(user_id text, idol_id text primary key , avatar text, bi_direction bit);

create table user_groups (user_id text primary key, group_id text);
create table group_users (group_id text primary key, user_id text)
