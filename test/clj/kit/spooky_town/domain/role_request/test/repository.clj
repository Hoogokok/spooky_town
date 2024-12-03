(ns kit.spooky-town.domain.role-request.test.repository
  (:require [kit.spooky-town.domain.role-request.repository.protocol :refer [RoleRequestRepository]]))

(defn save! [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-id-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-all-by-user [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-all-pending [_]
  (throw (ex-info "Not implemented" {})))

(defn update-request [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestRoleRequestRepository []
  RoleRequestRepository
  (save! [this request] (save! this request))
  (find-by-id [this id] (find-by-id this id))
  (find-by-uuid [this uuid] (find-by-uuid this uuid))
  (find-all-by-user [this user-id] (find-all-by-user this user-id))
  (find-all-pending [this] (find-all-pending this))
  (find-id-by-uuid [this uuid] (find-id-by-uuid this uuid))
  (update-request [this request] (update-request this request))) 