(ns kit.spooky-town.domain.user.gateway.token)

(defprotocol TokenGateway
  "토큰 관리를 위한 게이트웨이 프로토콜
   
   토큰 구조:
   - generate 시: {:user-id user-data, :exp timestamp}
   - verify 시: user-data (예: {:email string, :roles #{keyword}})"
  (generate [this id token-ttl]
    "사용자 아이디와 만료 시간으로 토큰을 생성")
  (verify [this token]
    "토큰을 검증하고 사용자 아이디를 반환")
  (revoke-token [this token]
    "토큰을 무효화"))
