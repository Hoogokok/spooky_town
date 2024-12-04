(ns kit.spooky-town.domain.user.gateway.email-verification)

(defprotocol EmailVerificationGateway
  (save-verification-status! [this email purpose status]
    "이메일 인증 상태를 저장합니다.
     status는 :pending, :verified 중 하나입니다.")
  
  (get-verification-status [this email purpose]
    "이메일의 인증 상태를 조회합니다.
     {:status :pending/:verified, :verified-at timestamp} 형태로 반환합니다.
     상태가 없으면 nil을 반환합니다.")
  
  (has-verified? [this email purpose]
    "해당 이메일이 특정 용도로 인증되었는지 확인합니다.")) 