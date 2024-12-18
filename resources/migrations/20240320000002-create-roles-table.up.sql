-- Role 테이블
CREATE TABLE IF NOT EXISTS roles (
  role_id VARCHAR(26) PRIMARY KEY,
  role_name VARCHAR(50) UNIQUE NOT NULL,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS user_roles (
  user_id VARCHAR(26) REFERENCES users(user_id),
  role_id VARCHAR(26) REFERENCES roles(role_id),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id)
);
INSERT INTO roles (role_id, role_name, description) VALUES
('01HQ5K4Y6Z0000000000000001', 'user', '일반 사용자'),
('01HQ5K4Y6Z0000000000000002', 'admin', '관리자'),
('01HQ5K4Y6Z0000000000000003', 'sns_publisher', 'SNS 게시자'),
('01HQ5K4Y6Z0000000000000004', 'content_creator', '컨텐츠 크리에이터'),
('01HQ5K4Y6Z0000000000000005', 'content_reviewer', '컨텐츠 리뷰어')
ON CONFLICT DO NOTHING;