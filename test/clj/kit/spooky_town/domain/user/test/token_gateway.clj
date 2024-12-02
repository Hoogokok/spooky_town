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

(defrecord TestTokenGateway []
  token/TokenGateway
  (generate [this user-id token-ttl] (generate this user-id token-ttl))
  (verify [this token] (verify this token))
  (revoke-token [this token] (revoke-token this token))) 