(ns kit.spooky-town.infrastructure.auth.jwt
  (:require [kit.spooky-town.domain.auth.core :as auth :refer [->AuthToken]]
            [buddy.sign.jwt :as jwt] 
            [kit.spooky_town.application.auth.service :refer [valid-credentials?]]
            [clj-time.core :as time]))

(defrecord JWTAuthService [config validator]
  auth/AuthenticationService
  (authenticate [this credentials]
    (when (valid-credentials? validator credentials)
      (let [claims {:email (:email credentials)
                   :exp (time/plus (time/now) (time/hours 24))}]
        (->AuthToken
         (jwt/sign claims (:secret config))
         (:exp claims)))))
  
  (verify-token [this token]
    (try
      (jwt/unsign token (:secret config))
      (catch Exception _
        nil))))

(defn create-jwt-auth-service [config validator]
  (->JWTAuthService config validator)) 