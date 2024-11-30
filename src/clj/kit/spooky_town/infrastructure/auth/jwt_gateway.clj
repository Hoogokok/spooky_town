(ns kit.spooky-town.infrastructure.auth.jwt-gateway
  (:require [kit.spooky-town.domain.auth.gateway :as gateway]
            [kit.spooky-town.domain.auth.core :as auth :refer [->AuthToken]]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]))

(defrecord JWTGateway [config]
  gateway/TokenGateway
  
  (create-token [this user-data]
    (try
      (let [claims {:user user-data
                    :exp (time/plus (time/now) 
                                  (time/hours (:token-expire-hours config)))}
            token (jwt/sign claims (:jwt-secret config) {:alg :hs512})]
        (->AuthToken token (:exp claims)))
      (catch Exception e
        (throw (ex-info "Token creation failed" 
                       {:type :token-creation-error
                        :cause (.getMessage e)})))))
  
  (verify-token [this token]
    (try
      (when-let [claims (jwt/unsign token (:jwt-secret config) {:alg :hs512})]
        (get claims :user))
      (catch Exception e
        (throw (ex-info "Token verification failed"
                       {:type :token-verification-error
                        :cause (.getMessage e)})))))
  
  (revoke-token [this token]
    ;; TODO: 토큰 블랙리스트 구현
    true))

(defn create-jwt-gateway [config]
  (->JWTGateway config)) 