(ns kit.spooky-town.domain.user.gateway.password)

(defprotocol PasswordGateway
  (hash-password [this password]
    "비밀번호를 해시화합니다.")
  (verify-password [this password hashed-password]
    "비밀번호가 해시와 일치하는지 확인합니다.")) 