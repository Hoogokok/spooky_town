-- Initialize database schema

-- Schema migrations table (자동 생성됨)
-- CREATE TABLE schema_migrations (
--     version varchar(255) PRIMARY KEY,
--     applied_at timestamp with time zone DEFAULT now()
-- );

-- Health check function
CREATE OR REPLACE FUNCTION health_check()
RETURNS INTEGER AS $$
BEGIN
    RETURN 1;
END;
$$ LANGUAGE plpgsql; 