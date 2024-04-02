CREATE KEYSPACE relationship
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use relationship;
//user -> followers
create table followers_by_user_id(user_id text , friend_id text not null , avatar text, group_id text,name text, primary key ( (user_id),friend_id));
create index on followers_by_user_id ((user_id));

//user -> fans
//create table followers_by_idol_id(idol_id text, user_id text , avatar text, group_id text, name text, primary key ( (idol_id, user_id) ));

// privacy group
create table users_in_groups(owner_id text, group_id text , member_id text , user_name text, user_avatar text, primary key ( (owner_id, member_id) ));

//
//create table user_owned_groups(user_id text, group_id text, group_name text, group_avatar text, count int, primary key ( (user_id, group_id) ));