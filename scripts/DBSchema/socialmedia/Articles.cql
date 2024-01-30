create table articles_by_article_id(article_id text primary key, author_id text, author_name text, last_modified timestamp, pictures list<text>, content text, access_type text, users set<text>);
create table articles_by_user_id(article_id text, author_id text primary key , author_name text, last_modified timestamp, pictures list<text>, content text, access_type text, users set<text>);



