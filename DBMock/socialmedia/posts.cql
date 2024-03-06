CREATE KEYSPACE articles
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use articles;



