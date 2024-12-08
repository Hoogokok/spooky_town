(ns kit.spooky-town.domain.user.value
  (:require [clojure.spec.alpha :as s]))

;; user-id (ULID)
(s/def ::user-id string?)

(defn create-user-id [id]
  (when (s/valid? ::user-id id)
    id))

;; UUID
(s/def ::uuid uuid?)

;; Email
(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? #(re-matches email-regex %)))

(defn create-email [address]
  (when (s/valid? ::email address)
    address))

;; Name (2-100자)
(s/def ::name (s/and string? #(>= (count %) 2) #(<= (count %) 100)))

(defn create-name [name]
  (when (s/valid? ::name name)
    name))

;; Password (해시된)
(s/def ::hashed-password string?)

;; Password (8자 이상, 특수문자 포함)
(def password-regex #"^(?=.*[!@#$%^&*(),.?\":{}|<>])(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
(s/def ::password (s/and string? #(re-matches password-regex %)))

(defn create-password [pwd]
  (when (s/valid? ::password pwd)
    pwd))

;; Timestamp
(s/def ::created-at inst?)
(s/def ::updated-at (s/nilable inst?))

(defn create-timestamp []
  (java.util.Date.))

(defn update-timestamp [entity]
  (assoc entity :updated-at (create-timestamp)))

;; 탈퇴 관련
(s/def ::deleted-at (s/nilable inst?))
(s/def ::withdrawal-reason (s/nilable string?))
