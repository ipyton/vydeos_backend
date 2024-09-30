CREATE KEYSPACE botChats
WITH REPLICATION = {
    'class' : 'SimpleStrategy',
    'replication_factor' : 1
    };
use botChats;


create table sessions(uuid text, sessionId uuid ,lastModified timestamp,  title text, primary key ( uuid, sessionId )); // sessionId:last_modified
create table chats(sessionId uuid, sendTime timestamp, role text, content text, primary key ( sessionId, sendTime ));


