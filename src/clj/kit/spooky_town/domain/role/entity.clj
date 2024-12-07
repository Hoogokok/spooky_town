(ns kit.spooky-town.domain.role.entity
  (:require [clojure.spec.alpha :as s]
            [kit.spooky-town.domain.role.value :as value]))

;; Entity Spec
(s/def ::role
  (s/keys :req-un [::value/role-id
                   ::value/role-name]
          :opt-un [::value/description
                   ::value/created-at
                   ::value/updated-at]))

(defrecord Role [role-id role-name description created-at updated-at])

(defn create-role
  [{:keys [role-id role-name description created-at]
    :or {created-at (value/create-timestamp)}}]
  (let [role (map->Role {:role-id role-id
                         :role-name (value/create-role-name role-name)
                         :description description
                         :created-at created-at
                         :updated-at created-at})]
    (when (s/valid? ::role role)
      role)))

;; Role 권한 체크 메서드들
(defn can-manage-content? [^Role role]
  (value/can-manage-content? (:role-name role)))

(defn can-publish-sns? [^Role role]
  (value/can-publish-sns? (:role-name role)))

(defn can-manage-system? [^Role role]
  (value/can-manage-system? (:role-name role)))

;; Role 업데이트 메서드들
(defn update-description
  [^Role role new-description]
  (when-let [validated-description (value/create-description new-description)]
    (map->Role (assoc role 
                     :description validated-description
                     :updated-at (value/create-timestamp))))) 