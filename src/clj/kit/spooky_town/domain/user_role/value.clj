(ns kit.spooky-town.domain.user-role.value
  (:require [clojure.spec.alpha :as s]))

;; Timestamps
(s/def ::created-at inst?)

(defn create-timestamp []
  (java.util.Date.))

;; User Role 관계
(s/def ::user-id string?)
(s/def ::role-id string?)

(defn create-user-role-relation [user-id role-id]
  (when (and (s/valid? ::user-id user-id)
             (s/valid? ::role-id role-id))
    {:user-id user-id
     :role-id role-id
     :created-at (create-timestamp)})) 