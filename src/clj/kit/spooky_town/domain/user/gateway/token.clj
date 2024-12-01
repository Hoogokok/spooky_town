(ns kit.spooky-town.domain.user.gateway.token)

(defprotocol TokenGateway
  (generate [this user-uuid token-ttl]
    "사용자 UUID와 토큰 만료 시간으로 토큰을 생성합니다.")) 