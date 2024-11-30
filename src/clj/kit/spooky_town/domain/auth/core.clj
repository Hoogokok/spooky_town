(ns kit.spooky-town.domain.auth.core)

;; 도메인 모델
(defrecord Credentials [email password])
(defrecord AuthToken [value expires-at])
(defrecord User [id email roles])

;; 핵심 도메인 프로토콜
(defprotocol AuthenticationDomain
  (authenticate [this credentials]
    "주어진 자격 증명으로 사용자를 인증하고 토큰을 반환합니다.")
  (verify-authentication [this token]
    "토큰의 유효성을 검증하고 사용자 정보를 반환합니다.")
  (authorize [this user permission]
    "사용자가 특정 권한을 가지고 있는지 확인합니다."))

;; 도메인 예외
(defrecord AuthenticationError [message])
(defrecord AuthorizationError [message])

;; 도메인 이벤트
(defrecord UserAuthenticated [user timestamp])
(defrecord AuthenticationFailed [credentials timestamp])