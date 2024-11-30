(ns kit.spooky-town.domain.auth.core)

(defprotocol AuthenticationService
  "인증 서비스 프로토콜"
  (authenticate [this credentials])
  (verify-token [this token]))

(defrecord Credentials [email password])
(defrecord AuthToken [value expires-at])