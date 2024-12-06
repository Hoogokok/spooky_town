(ns kit.spooky-town.domain.actor.test.repository
  (:require [kit.spooky-town.domain.actor.repository.protocol :refer [ActorRepository]]))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-ids [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-name [_ _]
  (throw (ex-info "Not implemented" {})))

(defn save! [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestActorRepository []
  ActorRepository
  (find-by-id [this id] (find-by-id this id))
  (find-by-ids [this ids] (find-by-ids this ids))
  (find-by-name [this name] (find-by-name this name))
  (save! [this actor] (save! this actor))) 