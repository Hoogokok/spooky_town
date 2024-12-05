-- :name health-check :? :1
-- :doc 데이터베이스 헬스 체크를 위한 쿼리
SELECT health_check() as status; 

-- :name create-user! :! :n
-- :doc 새로운 사용자를 생성합니다
INSERT INTO users (email, password_hash, name)
VALUES (:email, :password_hash, :name)
RETURNING user_id, uuid, email, name, created_at;

-- :name get-user-by-id :? :1
-- :doc ID로 사용자를 조회합니다 (내부용)
SELECT user_id, uuid, email, name, created_at, updated_at
FROM users
WHERE user_id = :user_id;

-- :name get-user-by-uuid :? :1
-- :doc UUID로 사용자를 조회합니다 (외부용)
SELECT user_id, uuid, email, name, created_at, updated_at
FROM users
WHERE uuid = :uuid;

-- :name get-user-by-email :? :1
-- :doc 이메일로 사용자를 조회합니다
SELECT user_id, uuid, email, password_hash, name, created_at, updated_at
FROM users
WHERE email = :email;

-- :name update-user! :! :n
-- :doc 사용자 정보를 업데이트합니다
UPDATE users
SET name = :name,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = :user_id
RETURNING user_id, uuid, email, name, updated_at;

-- :name delete-user! :! :n
-- :doc 사용자를 삭제합니다
DELETE FROM users
WHERE user_id = :user_id;

-- :name add-user-role! :! :n
-- :doc 사용자에게 역할을 추가합니다
INSERT INTO user_roles (user_id, role)
VALUES (:user_id, :role)
RETURNING user_id, role, created_at;

-- :name get-user-roles :? :*
-- :doc 사용자의 모든 역할을 조회합니다
SELECT role
FROM user_roles
WHERE user_id = :user_id;

-- :name remove-user-role! :! :n
-- :doc 사용자의 역할을 제거합니다
DELETE FROM user_roles
WHERE user_id = :user_id AND role = :role;

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
SELECT user_id
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