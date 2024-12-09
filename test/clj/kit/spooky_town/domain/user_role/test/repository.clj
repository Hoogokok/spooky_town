(ns kit.spooky-town.domain.user-role.test.repository
  (:require [kit.spooky-town.domain.user-role.repository.protocol :refer [UserRoleRepository]]))

(defn add-user-role! [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defn remove-user-role! [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defn find-roles-by-user [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-users-by-role [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestUserRoleRepository []
  UserRoleRepository
  (add-user-role! [this user-id role-id] (add-user-role! this user-id role-id))
  (remove-user-role! [this user-id role-id] (remove-user-role! this user-id role-id))
  (find-roles-by-user [this user-id] (find-roles-by-user this user-id))
  (find-users-by-role [this role-id] (find-users-by-role this role-id)))