CREATE KEYSPACE files
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

use files;


create table user_information(user_id text primary key, user_name text, avatar text, birthdate date,gender boolean, intro text, apps list<text>);

