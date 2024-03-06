CREATE KEYSPACE chat
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use chat;
