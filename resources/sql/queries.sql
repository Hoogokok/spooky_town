-- :name health-check :? :1
-- :doc 데이터베이스 헬스 체크를 위한 쿼리
SELECT health_check() as status; 

-- :name create-user! :! :n
-- :doc 새로운 사용자를 생성합니다
INSERT INTO users (email, password_hash, name)
VALUES (:email, :password_hash, :name)
RETURNING id, uuid, email, name, created_at;

-- :name get-user-by-id :? :1
-- :doc ID로 사용자를 조회합니다 (내부용)
SELECT id, uuid, email, name, created_at, updated_at
FROM users
WHERE id = :id;

-- :name get-user-by-uuid :? :1
-- :doc UUID로 사용자를 조회합니다 (외부용)
SELECT id, uuid, email, name, created_at, updated_at
FROM users
WHERE uuid = :uuid;

-- :name get-user-by-email :? :1
-- :doc 이메일로 사용자를 조회합니다
SELECT id, uuid, email, password_hash, name, created_at, updated_at
FROM users
WHERE email = :email;

-- :name update-user! :! :n
-- :doc 사용자 정보를 업데이트합니다
UPDATE users
SET name = :name,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :id
RETURNING id, uuid, email, name, updated_at;

-- :name delete-user! :! :n
-- :doc 사용자를 삭제합니다
DELETE FROM users
WHERE id = :id;

-- :name add-user-role! :! :n
-- :doc 사용자에게 역할을 추가합니다
INSERT INTO user_roles (user_id, role)
VALUES (:user_id, :role)
RETURNING id, user_id, role, created_at;

-- :name get-user-roles :? :*
-- :doc 사용자의 모든 역할을 조회합니다
SELECT role
FROM user_roles
WHERE user_id = :user_id;

-- :name remove-user-role! :! :n
-- :doc 사용자의 역할을 제거합니다
DELETE FROM user_roles
WHERE user_id = :user_id AND role = :role;

-- :name create-session! :! :n
-- :doc 새로운 사용자 세션을 생성합니다
INSERT INTO user_sessions (user_id, token, expires_at)
VALUES (:user_id, :token, :expires_at)
RETURNING id, user_id, token, created_at, expires_at;

-- :name get-session :? :1
-- :doc 토큰으로 세션을 조회합니다
SELECT s.id, s.user_id, s.token, s.created_at, s.expires_at,
       u.uuid as user_uuid, u.email, u.name
FROM user_sessions s
JOIN users u ON u.id = s.user_id
WHERE s.token = :token AND s.expires_at > CURRENT_TIMESTAMP;

-- :name delete-session! :! :n
-- :doc 세션을 삭제합니다
DELETE FROM user_sessions
WHERE token = :token;

-- :name delete-expired-sessions! :! :n
-- :doc 만료된 세션을 모두 삭제합니다
DELETE FROM user_sessions
WHERE expires_at <= CURRENT_TIMESTAMP;