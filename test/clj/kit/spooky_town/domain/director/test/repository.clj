(ns kit.spooky-town.domain.director.test.repository
  (:require [kit.spooky-town.domain.director.repository.protocol :refer [DirectorRepository]]))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-ids [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-name [_ _]
  (throw (ex-info "Not implemented" {})))

(defn save! [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestDirectorRepository []
  DirectorRepository
  (find-by-id [this id] (find-by-id this id))
  (find-by-ids [this ids] (find-by-ids this ids))
  (find-by-name [this name] (find-by-name this name))
  (save! [this director] (save! this director))) 