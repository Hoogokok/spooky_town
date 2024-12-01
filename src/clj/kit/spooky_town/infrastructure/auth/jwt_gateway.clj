(ns kit.spooky-town.infrastructure.auth.jwt-gateway
  (:require [kit.spooky-town.domain.auth.model :as model]
            [kit.spooky-town.domain.auth.gateway :as gateway]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [integrant.core :as ig]
            [failjure.core :as f]))

(defrecord JWTGateway [config]
  gateway/TokenGateway

  (create-token [this user-data]
    (try
      (let [claims {:user user-data
                    :exp (time/plus (time/now)
                                    (time/hours (:token-expire-hours config)))}
            token (jwt/sign claims (:jwt-secret config) {:alg :hs512})]
        (model/->AuthToken token (:exp claims)))
      (catch Exception e
        (f/fail (str "Token creation failed: " (.getMessage e))))))

  (verify-token [this token]
    (try
      (let [token-str (if (instance? kit.spooky_town.domain.auth.model.AuthToken token)
                       (:value token)
                       token)]
        (when-let [claims (jwt/unsign token-str (:jwt-secret config) {:alg :hs512})]
          (let [user (get claims :user)]
            (update user :roles #(set (map keyword %))))))
      (catch Exception e
        (f/fail (str "Token verification failed: " (.getMessage e))))))

  (revoke-token [this token]
    true))

(defmethod ig/init-key :auth/jwt [_ config]
  (->JWTGateway config))