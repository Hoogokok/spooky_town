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

(defrecord TestUserRepository []

  UserRepository
  (save! [this user] (save! this user))
  (find-by-id [this user-id] (find-by-id this user-id))
  (find-by-email [this email] (find-by-email this email))
  (find-by-uuid [this uuid] (find-by-uuid this uuid)))
