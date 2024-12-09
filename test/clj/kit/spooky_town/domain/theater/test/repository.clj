(ns kit.spooky-town.domain.theater.test.repository
  (:require [kit.spooky-town.domain.theater.repository.protocol :refer [TheaterRepository]]))

(defn save! [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-id-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-chain-type [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestTheaterRepository []
  TheaterRepository
  (save! [this theater] (save! this theater))
  (find-by-id [this id] (find-by-id this id))
  (find-by-uuid [this uuid] (find-by-uuid this uuid))
  (find-id-by-uuid [this uuid] (find-id-by-uuid this uuid))
  (find-by-chain-type [this chain-type] (find-by-chain-type this chain-type))) 