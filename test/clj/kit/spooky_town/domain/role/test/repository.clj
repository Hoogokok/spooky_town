(ns kit.spooky-town.domain.role.test.repository
  (:require [kit.spooky-town.domain.role.repository.protocol :refer [RoleRepository]]))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-name [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-all [_]
  (throw (ex-info "Not implemented" {})))

(defrecord TestRoleRepository []
  RoleRepository
  (find-by-id [this id] (find-by-id this id))
  (find-by-name [this name] (find-by-name this name))
  (find-all [this] (find-all this))) 