CREATE KEYSPACE ui
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use ui;

create table sidebar(
    id int,
    name text,
    path text,
    icon_name text, primary key ( id )
);





