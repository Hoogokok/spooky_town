(ns kit.spooky-town.domain.user.value
  (:require [clojure.spec.alpha :as s]))

;; Email
(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? #(re-matches email-regex %)))

(defn create-email [address]
  (when (s/valid? ::email address)
    address))

;; Password (8자 이상, 특수문자 포함)
(def password-regex #"^(?=.*[!@#$%^&*(),.?\":{}|<>])(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
(s/def ::password (s/and string? #(re-matches password-regex %)))

(defn create-password [pwd]
  (when (s/valid? ::password pwd)
    pwd))

;; Hashed Password (해시된 비밀번호)
(s/def ::hashed-password string?)

;; Name (2-100자)
(s/def ::name (s/and string? #(>= (count %) 2) #(<= (count %) 100)))

(defn create-name [name]
  (when (s/valid? ::name name)
    name))

;; Role
(s/def ::role #{:user :admin :moderator})
(s/def ::roles (s/coll-of ::role :kind set?))

(defn create-roles
  ([] #{:user})  ;; 기본 역할
  ([roles] (when (s/valid? ::roles roles)
             roles)))

(defn add-role [roles role]
  (when (s/valid? ::role role)
    (conj roles role)))

(defn remove-role [roles role]
  (when (s/valid? ::role role)
    (disj roles role)))

(defn has-role? [roles role]
  (contains? roles role))

;; ID
(s/def ::id pos-int?)
(s/def ::uuid uuid?)

(defn create-uuid []
  (random-uuid))

;; Timestamp
(s/def ::created-at inst?)
(s/def ::updated-at (s/nilable inst?))  ;; nilable로 변경

(defn create-timestamp []
  (java.util.Date.))

(defn update-timestamp [entity]
  (assoc entity :updated-at (create-timestamp)))
