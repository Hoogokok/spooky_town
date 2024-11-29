CREATE TABLE IF NOT EXISTS schema_migrations
(version varchar(255) primary key,
 applied_at timestamp with time zone default now()); 