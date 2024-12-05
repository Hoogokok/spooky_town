(ns kit.spooky-town.domain.role.entity
  (:require [clojure.spec.alpha :as s]))

;; Entity Spec
(s/def ::role-id int?)
(s/def ::role-name string?)

(s/def ::role
  (s/keys :req-un [::role-id
                   ::role-name]))

(defrecord Role [role-id role-name])

(defn create-role
  [{:keys [role-id role-name]}]
  (let [role (map->Role {:role-id role-id
                        :role-name role-name})]
    (when (s/valid? ::role role)
      role))) 