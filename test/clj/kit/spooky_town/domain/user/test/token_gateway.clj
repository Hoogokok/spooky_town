(ns kit.spooky-town.domain.user.test.token-gateway
  (:require [kit.spooky-town.domain.user.gateway.token :as token]))

(defn generate [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestTokenGateway []
  token/TokenGateway
  (generate [this user-uuid token-ttl] (generate this user-uuid token-ttl))) 