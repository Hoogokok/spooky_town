(ns kit.spooky-town.domain.auth.test.authorization
  (:require [kit.spooky-town.domain.auth.authorization.protocol :refer [UserAuthorization]]))

(defn has-permission? [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestUserAuthorization []
  UserAuthorization
  (has-permission? [this user-uuid permission]
    (has-permission? this user-uuid permission)))

