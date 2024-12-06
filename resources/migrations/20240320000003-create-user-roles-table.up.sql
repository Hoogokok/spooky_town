-- User roles table
CREATE TABLE IF NOT EXISTS user_roles (
  user_id VARCHAR(26) REFERENCES users(user_id),
  role_id INT REFERENCES roles(role_id),
  PRIMARY KEY (user_id, role_id)
); 