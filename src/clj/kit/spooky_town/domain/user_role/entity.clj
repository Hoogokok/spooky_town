(ns kit.spooky-town.domain.user-role.entity
  (:require [clojure.spec.alpha :as s]
            [kit.spooky-town.domain.user-role.value :as value]))

;; UserRole 레코드 정의
(defrecord UserRole [user-id role-id created-at])

;; UserRole 스펙 정의
(s/def ::user-role
  (s/keys :req-un [::value/user-id
                   ::value/role-id
                   ::value/created-at]))

;; UserRole 생성
(defn create-user-role [{:keys [user-id role-id created-at]
                         :or {created-at (value/create-timestamp)}}]
  (let [user-role (map->UserRole {:user-id user-id
                                 :role-id role-id
                                 :created-at created-at})]
    (when (s/valid? ::user-role user-role)
      user-role)))

;; 역할 검증 함수들
(defn has-role? [user-roles role-name]
  (some #(= role-name (:role-name %)) user-roles))

(defn can-manage-system? [roles]
  (has-role? roles :admin)) 