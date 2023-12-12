//user -> followers
create table followers_by_user_id(user_id text primary key , friend_id text primary key , avatar text, group_id text, bi_direction int);

//user -> fans
create table followers_by_idol_id(idol_id text primary key, user_id text primary key , avatar text, group_id text, bi_direction int);

// group -> users
create table user_groups (group_id text primary key ,name text, type text,friend_id text primary key , avatar text, introduction text, config map<text, text>);

// user -> groups
create table user_owned_groups(user_id text primary key, group_id text primary key , group_text text, group_avatar text);
