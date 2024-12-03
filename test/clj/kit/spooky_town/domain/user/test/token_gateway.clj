(ns kit.spooky-town.domain.user.test.token-gateway
  (:require [kit.spooky-town.domain.user.gateway.token :as token]
            [failjure.core :as f]))

(defn generate [_ user-id token-ttl]
  "generated_token")

(defn verify [_ token]
  (if (= token "valid-token")
    1
    (f/fail :token-error/invalid)))

(defn revoke-token [_ token]
  (if (= token "valid-token")
    true
    (f/fail :token-error/invalid)))

(defn find-valid-token [_ user-uuid]
  nil)

(defn check-rate-limit [_ key action]
  false)

(defrecord TestTokenGateway []
  token/TokenGateway
  (generate [this user-id token-ttl] (generate this user-id token-ttl))
  (verify [this token] (verify this token))
  (revoke-token [this token] (revoke-token this token))
  (find-valid-token [this user-uuid] (find-valid-token this user-uuid))
  (check-rate-limit [this key action] (check-rate-limit this key action))) 