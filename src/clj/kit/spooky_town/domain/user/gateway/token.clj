(ns kit.spooky-town.domain.user.gateway.token)

(defprotocol TokenGateway
  (generate [this user-id token-ttl]
    "사용자 ID와 만료 시간으로 새로운 토큰을 생성합니다.
     성공 시 token 반환")
  
  (verify [this token]
    "토큰을 검증하고 사용자 ID를 추출합니다.
     성공 시 user-id 반환, 실패 시 (f/fail error-message) 반환")
  
  (revoke-token [this token]
    "토큰을 무효화합니다.
     성공 시 true 반환, 실패 시 (f/fail error-message) 반환"))
