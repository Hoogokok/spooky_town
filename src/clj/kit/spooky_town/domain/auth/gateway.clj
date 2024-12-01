(ns kit.spooky-town.domain.auth.gateway)

(defprotocol TokenGateway
  "외부 토큰 서비스와의 통신을 추상화하는 프로토콜"
  
  (create-token [this user-data]
    "사용자 데이터로부터 새로운 인증 토큰을 생성합니다.
     성공 시 (f/ok token), 실패 시 (f/fail error-message) 반환")
  
  (verify-token [this token]
    "토큰의 유효성을 검증하고 포함된 사용자 데이터를 반환합니다.
     성공 시 (f/ok user-data), 실패 시 (f/fail error-message) 반환")
  
  (revoke-token [this token]
    "토큰을 무효화합니다.
     성공 시 (f/ok true), 실패 시 (f/fail error-message) 반환"))

;; 도메인 이벤트
(defrecord TokenCreated [token user-data timestamp])
(defrecord TokenVerified [token result timestamp])
(defrecord TokenRevoked [token timestamp]) 