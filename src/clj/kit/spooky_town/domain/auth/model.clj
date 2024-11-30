(ns kit.spooky-town.domain.auth.model)

;; 인증 토큰 모델
(defrecord AuthToken [value expires-at])

;; 인증 세션 모델
(defrecord AuthSession [token user-id created-at expires-at])

;; 인증 결과 모델
(defrecord AuthResult [success? token error])

;; 도메인 예외
(defrecord AuthenticationError [message])
(defrecord TokenValidationError [message])
