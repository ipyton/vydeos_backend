create table comments_by_user_id(user_id text primary key, comment_id text, date timestamp, content text, type text, attributes map<text, text>);
create table comments_by_comment(comment_refer text primary key, comment_id text, date timestamp, content text, type text, attributes map<text, text>);

create table articles_by_article_id(article_id text primary key, author_id text, author_name text, last_modified timestamp, pictures list<text>, content text);
create table articles_by_user_id(article_id text, author_id text primary key , author_name text, last_modified timestamp, pictures list<text>, content text);



