(ns kit.spooky-town.infrastructure.auth.jwt-gateway
  (:require [kit.spooky-town.domain.user.gateway.token :as token]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [integrant.core :as ig]
            [failjure.core :as f]))

(defrecord JWTGateway [config]
  token/TokenGateway
  
  (generate [this user-data token-ttl]
    (try
      (let [claims {:user-id user-data
                   :exp (time/plus (time/now)
                                 (time/hours (or token-ttl 
                                               (:token-expire-hours config))))}]
        (jwt/sign claims (:jwt-secret config) {:alg :hs512}))
      (catch Exception e
        (f/fail (str "Token generation failed: " (.getMessage e))))))

  (verify [this token]
    (try
      (let [claims (jwt/unsign token (:jwt-secret config) {:alg :hs512})]
        (-> claims
            :user-id
            (update :roles #(set (map keyword %)))))
      (catch Exception e
        (f/fail (str "Token verification failed: " (.getMessage e))))))

  (revoke-token [this token]
    true))

(defmethod ig/init-key :infrastructure/jwt-gateway [_ config]
  (->JWTGateway config))