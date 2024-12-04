(ns kit.spooky-town.infrastructure.auth.bcrypt-gateway
  (:require [kit.spooky-town.domain.user.gateway.password :refer [PasswordGateway]]
            [buddy.hashers :as hashers]
            [integrant.core :as ig]))

(defrecord BCryptPasswordGateway []
  PasswordGateway
  
  (hash-password [_ password]
    (hashers/derive password {:alg :bcrypt+sha512}))
  
  (verify-password [_ password hashed-password]
    (hashers/check password hashed-password)))

;; Integrant init-key 메서드 추가
(defmethod ig/init-key :auth/bcrypt-password-gateway
  [_ _]
  (->BCryptPasswordGateway)) 