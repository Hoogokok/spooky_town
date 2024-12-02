(ns kit.spooky-town.domain.user.gateway.token)

(defprotocol TokenGateway
  (generate [this id token-ttl]
            "사용자 ID와 토큰 만료 시간으로 토큰을 생성합니다.")
  (verify [this token]
    "토큰을 검증합니다.")
  (get-user-id [this token]
    "토큰에서 사용자 ID를 추출합니다.")
  (get-expiry [this token]
    "토큰의 만료 시간을 반환합니다."))
