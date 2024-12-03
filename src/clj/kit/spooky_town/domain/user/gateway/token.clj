(ns kit.spooky-town.domain.user.gateway.token)

(defprotocol TokenGateway
  (generate [this user-uuid ttl]
    "토큰을 생성합니다.")
  (verify [this token]
    "토큰을 검증하고 user-uuid를 반환합니다.")
  (revoke-token [this token]
    "토큰을 무효화합니다.")
  (find-valid-token [this user-uuid]
    "사용자의 유효한 토큰을 찾습니다.")
  (check-rate-limit [this key action]
    "특정 액션에 대한 rate limit을 체크합니다."))
