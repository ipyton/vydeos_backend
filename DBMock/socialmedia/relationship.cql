CREATE KEYSPACE relationship
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use relationship;
//user -> followers
create table followers_by_user_id(user_id text primary key , friend_id text primary key , avatar text, group_id text, bi_direction int);

//user -> fans
create table followers_by_idol_id(idol_id text primary key, user_id text primary key , avatar text, group_id text, bi_direction int);

// group -> users
create table users_in_groups(owner_id text primary key, group_id text primary key, user_id text primary key, user_name text, user_avatar text);

//
create table user_owned_groups(user_id text primary key, group_id text primary key, group_name text, group_avatar text, count int);