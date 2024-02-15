CREATE KEYSPACE file
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use file;
create table upload_files_status(file_hash text primary key, owner_name text, total int, current int, start_time timestamp);
