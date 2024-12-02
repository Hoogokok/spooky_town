(ns kit.spooky-town.domain.user.test.token-gateway
  (:require [kit.spooky-town.domain.user.gateway.token :as token]))

(defn generate [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defn verify [_ _]
  (throw (ex-info "Not implemented" {})))

(defn get-user-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn get-expiry [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestTokenGateway []
  token/TokenGateway
  (generate [this id token-ttl] (generate this id token-ttl))
  (verify [this token] (verify this token))
  (get-user-id [this token] (get-user-id this token))
  (get-expiry [this token] (get-expiry this token))) 