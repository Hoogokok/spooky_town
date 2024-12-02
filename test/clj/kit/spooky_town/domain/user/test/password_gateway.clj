(ns kit.spooky-town.domain.user.test.password-gateway
  (:require [kit.spooky-town.domain.user.gateway.password :as password]))

(defn hash-password [_ _]
  (throw (ex-info "Not implemented" {})))

(defn verify-password [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestPasswordGateway []
  password/PasswordGateway
  (hash-password [this password] (hash-password this password))
  (verify-password [this password hashed-password] (verify-password this password hashed-password))) 