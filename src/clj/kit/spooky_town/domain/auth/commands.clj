(ns kit.spooky-town.domain.auth.commands
  (:require [kit.spooky-town.domain.auth.model :as model]
            [kit.spooky-town.domain.auth.gateway :as gateway]
            [kit.spooky-town.domain.auth.utils :as utils]
            [failjure.core :as f]))

;; Commands
(defrecord Authenticate [email password])
(defrecord RevokeToken [token])

;; Events
(defrecord UserAuthenticated [user token timestamp])
(defrecord AuthenticationFailed [email timestamp error])
(defrecord TokenRevoked [token timestamp])

;; Command Handlers
(defn authenticate [{:keys [email password]} auth-gateway]
  (let [result (gateway/create-token auth-gateway {:email email})]
    (if (f/failed? result)
      (->AuthenticationFailed email (utils/now) (f/message result))
      (->UserAuthenticated {:email email} result (utils/now)))))

(defn revoke-token [{:keys [token]} auth-gateway]
  (let [result (gateway/revoke-token auth-gateway token)]
    (if (f/failed? result)
      (model/->TokenValidationError (f/message result))
      (->TokenRevoked token (utils/now)))))