(ns kit.spooky-town.domain.user.gateway.email)

(defprotocol EmailGateway
  (send-verification-email [this email token]
    "회원가입 이메일 인증 메일을 발송합니다.")
  
  (send-password-reset-email [this email token]
    "비밀번호 초기화 메일을 발송합니다.")
  
  (send-email-change-verification [this email token]
    "이메일 변경 인증 메일을 발송합니다.")) 