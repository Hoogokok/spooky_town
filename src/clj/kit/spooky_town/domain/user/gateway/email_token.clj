(ns kit.spooky-town.domain.user.gateway.email-token)

(defprotocol EmailTokenGateway
  (generate-token [this email purpose]
    "주어진 용도(가입/초기화/변경)에 맞는 이메일 인증 토큰을 생성합니다.")
  
  (verify-token [this token]
    "토큰을 검증하고 이메일과 용도를 반환합니다.")) 