(ns kit.spooky-town.domain.auth.queries
  (:require [kit.spooky-town.domain.auth.model :as model]
            [kit.spooky-town.domain.auth.gateway :as gateway]
            [failjure.core :as f]))

;; Queries
(defrecord VerifyToken [token])
(defrecord GetCurrentSession [token])

;; Query Handlers
(defn verify-token [{:keys [token]} auth-gateway]
  (let [result (gateway/verify-token auth-gateway token)]
    (if (f/failed? result)
      (model/->TokenValidationError (f/message result))
      result)))

(defn get-current-session [{:keys [token]} auth-gateway]
  (let [result (gateway/verify-token auth-gateway token)]
    (if (f/failed? result)
      (model/->TokenValidationError (f/message result))
      (model/->AuthSession token 
                          (:id result)
                          (:created-at result)
                          (:expires-at result)))))