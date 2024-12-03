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

-- :name insert-role-request! :! :n
-- :doc 새로운 역할 요청을 삽입합니다
INSERT INTO role_requests (uuid, user_id, requested_role, reason, status)
VALUES (:uuid, :user_id, :requested_role, :reason, :status);

-- :name find-role-request-by-id :? :1
-- :doc ID로 역할 요청을 조회합니다
SELECT * FROM role_requests WHERE id = :id;

-- :name find-role-request-by-uuid :? :1
-- :doc UUID로 역할 요청을 조회합니다
SELECT * FROM role_requests WHERE uuid = :uuid;

-- :name find-all-role-requests-by-user :? :*
-- :doc 사용자 ID로 모든 역할 요청을 조회합니다
SELECT * FROM role_requests WHERE user_id = :user_id ORDER BY created_at DESC;

-- :name find-all-pending-role-requests :? :*
-- :doc 모든 대기 중인 역할 요청을 조회합니다
SELECT * FROM role_requests WHERE status = 'pending' ORDER BY created_at ASC;

-- :name update-role-request! :! :n
-- :doc 역할 요청을 업데이트합니다
UPDATE role_requests
SET status = :status, updated_at = :updated_at, approved_by = :approved_by, rejected_by = :rejected_by, rejection_reason = :rejection_reason
WHERE id = :id;

-- :name find-user-id-by-uuid :? :1
-- :doc UUID로 사용자 ID 조회
SELECT id
FROM users
WHERE uuid = :uuid

-- :name find-role-request-id-by-uuid :? :1
-- :doc UUID로 역할 변경 요청 ID 조회
SELECT id
FROM role_requests
WHERE uuid = :uuid

-- :name mark-user-as-withdrawn :! :n
-- :doc 사용자를 탈퇴 처리합니다
UPDATE users
SET deleted_at = :deleted_at,
    withdrawal_reason = :withdrawal_reason
WHERE uuid = :uuid