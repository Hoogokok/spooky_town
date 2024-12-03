(ns kit.spooky-town.infrastructure.auth.bcrypt-gateway
  (:require [kit.spooky-town.domain.user.gateway.password :refer [PasswordGateway]]
            [buddy.hashers :as hashers]))

(defrecord BCryptPasswordGateway []
  PasswordGateway
  
  (hash-password [_ password]
    (hashers/derive password {:alg :bcrypt+sha512}))
  
  (verify-password [_ password hashed-password]
    (hashers/check password hashed-password))) 