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
-- :doc 사용자에게 역할을 할당합니다
INSERT INTO user_roles (user_id, role_id)
VALUES (:user_id, :role_id)
RETURNING user_id, role_id, created_at;

-- :name get-user-roles :? :*
-- :doc 사용자의 모든 역할을 조회합니다
SELECT r.*
FROM roles r
JOIN user_roles ur ON r.role_id = ur.role_id
WHERE ur.user_id = :user-id;

-- :name remove-user-role! :! :n
-- :doc 사용자의 역할을 제거합니다
DELETE FROM user_roles
WHERE user_id = :user-id AND role_id = :role-id;

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

-- :name save-movie! :! :n
-- :doc 새로운 영화를 생성합니다
INSERT INTO movies (movie_id, uuid, title, description, release_date)
VALUES (:movie_id, :uuid, :title, :description, :release_date)
RETURNING movie_id, uuid, title, description, release_date, created_at;

-- :name get-movie-by-id :? :1
-- :doc ID로 영화를 조회합니다
SELECT movie_id, uuid, title, description, release_date, created_at, updated_at
FROM movies
WHERE movie_id = :movie_id AND deleted_at IS NULL;

-- :name get-movie-by-uuid :? :1
-- :doc UUID로 영화를 조회합니다
SELECT movie_id, uuid, title, description, release_date, created_at, updated_at
FROM movies
WHERE uuid = :uuid AND deleted_at IS NULL;

-- :name mark-movie-as-deleted! :! :n
-- :doc 영화를 소프트 삭제 처리합니다
UPDATE movies
SET deleted_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE movie_id = :movie_id
RETURNING movie_id, uuid, deleted_at;

-- 감독 관련 쿼리
-- :name save-director! :! :n
-- :doc 새로운 감독을 생성합니다
INSERT INTO directors (director_id, uuid, name, birth_date)
VALUES (:director_id, :uuid, :name, :birth_date)
RETURNING director_id, uuid, name, birth_date, created_at;

-- :name get-director-by-id :? :1
-- :doc ID로 감독을 조회합니다
SELECT director_id, uuid, name, birth_date, created_at, updated_at
FROM directors
WHERE director_id = :director_id AND deleted_at IS NULL;

-- :name get-director-by-name :? :*
-- :doc 이름으로 감독을 조회합니다
SELECT director_id, uuid, name, birth_date, created_at, updated_at
FROM directors
WHERE name = :name AND deleted_at IS NULL;

-- 감독-영화 관계 쿼리
-- :name save-director-movie! :! :n
-- :doc 감독과 영화의 관계를 저장합니다
INSERT INTO director_movies (director_id, movie_id)
VALUES (:director_id, :movie_id)
RETURNING director_id, movie_id, created_at;

-- :name get-movies-by-director-id :? :*
-- :doc 감독이 연출한 영화 목록을 조회합니다
SELECT m.*
FROM movies m
JOIN director_movies dm ON m.movie_id = dm.movie_id
WHERE dm.director_id = :director_id AND m.deleted_at IS NULL;

-- :name get-directors-by-movie-id :? :*
-- :doc 영화의 감독 목록을 조회합니다
SELECT d.*
FROM directors d
JOIN director_movies dm ON d.director_id = dm.director_id
WHERE dm.movie_id = :movie_id AND d.deleted_at IS NULL;

-- 배우 관련 쿼리
-- :name save-actor! :! :n
-- :doc 새로운 배우를 생성합니다
INSERT INTO actors (actor_id, uuid, name, birth_date)
VALUES (:actor_id, :uuid, :name, :birth_date)
RETURNING actor_id, uuid, name, birth_date, created_at;

-- :name get-actor-by-id :? :1
-- :doc ID로 배우를 조회합니다
SELECT actor_id, uuid, name, birth_date, created_at, updated_at
FROM actors
WHERE actor_id = :actor_id AND deleted_at IS NULL;

-- :name get-actor-by-name :? :*
-- :doc 이름으로 배우를 조회합니다
SELECT actor_id, uuid, name, birth_date, created_at, updated_at
FROM actors
WHERE name = :name AND deleted_at IS NULL;

-- 영화-배우 관계 쿼리
-- :name save-movie-actor! :! :n
-- :doc 영화와 배우의 관계를 저장합니다
INSERT INTO movie_actors (movie_id, actor_id, role_name)
VALUES (:movie_id, :actor_id, :role_name)
RETURNING movie_id, actor_id, role_name, created_at;

-- :name get-actors-by-movie-id :? :*
-- :doc 영화의 출연 배우 목록을 조회합니다
SELECT a.*, ma.role_name
FROM actors a
JOIN movie_actors ma ON a.actor_id = ma.actor_id
WHERE ma.movie_id = :movie_id AND a.deleted_at IS NULL;

-- :name get-movies-by-actor-id :? :*
-- :doc 배우의 출연 영화 목록을 조회합니다
SELECT m.*, ma.role_name
FROM movies m
JOIN movie_actors ma ON m.movie_id = ma.movie_id
WHERE ma.actor_id = :actor_id AND m.deleted_at IS NULL;

-- Role 관련 쿼리
-- :name save-role! :! :n
-- :doc 새로운 역할을 생성합니다
INSERT INTO roles (role_id, role_name, description)
VALUES (:role-id, :role-name, :description)
RETURNING role_id, role_name, description, created_at;

-- :name get-role-by-id :? :1
-- :doc ID로 역할을 조회합니다
SELECT role_id, role_name, description, created_at, updated_at
FROM roles
WHERE role_id = :role-id;

-- :name get-role-by-name :? :1
-- :doc 이름으로 역할을 조회합니다
SELECT role_id, role_name, description, created_at, updated_at
FROM roles
WHERE role_name = :role-name;

-- :name get-all-roles :? :*
-- :doc 모든 역할을 조회합니다
SELECT role_id, role_name, description, created_at, updated_at
FROM roles
ORDER BY created_at ASC;

-- :name get-role-users :? :*
-- :doc 특정 역할을 가진 모든 사용자를 조회합니다
SELECT u.*
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
WHERE ur.role_id = :role-id AND u.deleted_at IS NULL;

-- 극장 관련 쿼리
-- :name save-theater! :! :n
-- :doc 새로운 극장을 생성합니다
INSERT INTO theaters (theater_id, uuid, chain_type, created_at, updated_at)
VALUES (:theater_id, :uuid, :chain_type, :created_at, :updated_at)
RETURNING theater_id, uuid, chain_type, created_at;

-- :name get-theater-by-id :? :1
-- :doc ID로 극장을 조회합니다 (내부용)
SELECT theater_id, uuid, chain_type, created_at, updated_at
FROM theaters
WHERE theater_id = :theater_id;

-- :name get-theater-by-uuid :? :1
-- :doc UUID로 극장을 조회합니다 (외부용)
SELECT theater_id, uuid, chain_type, created_at, updated_at
FROM theaters
WHERE uuid = :uuid;

-- :name find-theaters-by-chain-type :? :*
-- :doc 체인 타입으로 극장들을 조회합니다
SELECT theater_id, uuid, chain_type, created_at, updated_at
FROM theaters
WHERE chain_type = :chain_type;

-- 영화-극장 관계 쿼리
-- :name save-movie-theater! :! :n
-- :doc 영화-극장 관계를 저장합니다
INSERT INTO movie_theaters (movie_id, theater_id, created_at)
VALUES (:movie_id, :theater_id, :created_at)
RETURNING movie_id, theater_id, created_at;

-- :name get-theaters-by-movie :? :*
-- :doc 영화의 상영 극장 목록을 조회합니다
SELECT t.*
FROM theaters t
JOIN movie_theaters mt ON t.theater_id = mt.theater_id
WHERE mt.movie_id = :movie_id;

-- :name get-theaters-by-movies :? :*
-- :doc 여러 영화의 상영 극장 목록을 한 번에 조회합니다 (N+1 방지)
SELECT t.*, mt.movie_id
FROM theaters t
JOIN movie_theaters mt ON t.theater_id = mt.theater_id
WHERE mt.movie_id = ANY(:movie_ids::varchar[]);

-- :name get-movies-by-theater :? :*
-- :doc 극장의 상영 영화 목록을 조회합니다
SELECT m.*
FROM movies m
JOIN movie_theaters mt ON m.movie_id = mt.movie_id
WHERE mt.theater_id = :theater_id
  AND m.deleted_at IS NULL;

-- :name delete-movie-theater! :! :n
-- :doc 영화-극장 관계를 삭제합니다
DELETE FROM movie_theaters
WHERE movie_id = :movie_id AND theater_id = :theater_id;