ALTER TABLE movies 
ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL;

CREATE INDEX idx_movies_deleted_at ON movies(deleted_at); 