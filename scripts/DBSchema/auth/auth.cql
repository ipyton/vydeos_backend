CREATE KEYSPACE userInfo
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };
use userInfo;

create table user_auth (userId text, password text, email text, telephone text, primary key ( userId ));
create table user_tokens (user_token text primary key , userId text,  invalid_date timestamp);

