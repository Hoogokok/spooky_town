(ns kit.spooky-town.domain.user.test.repository
  (:require [kit.spooky-town.domain.user.repository.protocol :as user-repository :refer [UserRepository]]))

(defn save! [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))


(defn find-by-email [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-id-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn mark-as-withdrawn [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestUserRepository []

  UserRepository
  (save! [this user] (save! this user))
  (find-by-id [this id] (find-by-id this id))
  (find-by-email [this email] (find-by-email this email))
  (find-by-uuid [this uuid] (find-by-uuid this uuid))
  (find-id-by-uuid [this uuid] (find-id-by-uuid this uuid))
  (mark-as-withdrawn [this user] (mark-as-withdrawn this user)))