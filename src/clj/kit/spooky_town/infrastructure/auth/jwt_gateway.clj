(ns kit.spooky-town.infrastructure.auth.jwt-gateway
  (:require [kit.spooky-town.domain.user.gateway.token :as token]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [integrant.core :as ig]
            [failjure.core :as f]))

(defrecord JWTGateway [config]
  token/TokenGateway
  
  (generate [this id token-ttl]
    (try
      (let [claims {:user-id id
                   :exp (time/plus (time/now)
                                 (time/hours (or token-ttl 
                                               (:token-expire-hours config))))}]
        (jwt/sign claims (:jwt-secret config) {:alg :hs512}))
      (catch Exception e
        (f/fail (str "Token generation failed: " (.getMessage e))))))

  (verify [this token]
    (try
      (when-let [claims (jwt/unsign token (:jwt-secret config) {:alg :hs512})]
        (:user-id claims))
      (catch Exception e
        (f/fail (str "Token verification failed: " (.getMessage e))))))

  (revoke-token [this token]
    ;; JWT는 서버 측에서 개별 토큰 무효화가 어려움
    ;; 실제 무효화가 필요하다면 토큰 블랙리스트 구현 필요
    true))

(defmethod ig/init-key :infrastructure/jwt-gateway [_ config]
  (->JWTGateway config))