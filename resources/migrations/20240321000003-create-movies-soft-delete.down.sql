DROP INDEX IF EXISTS idx_movies_deleted_at;
ALTER TABLE movies DROP COLUMN IF EXISTS deleted_at; 