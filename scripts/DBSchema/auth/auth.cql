CREATE KEYSPACE userInfo
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use userInfo;

create table user_auth (userId text, password text, email text, telephone text, primary key ( userId ));