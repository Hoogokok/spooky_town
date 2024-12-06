(ns kit.spooky-town.domain.movie.test.repository
  (:require [kit.spooky-town.domain.movie.repository.protocol :refer [MovieRepository]]))

(defn save! [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-criteria [_ _]
  (throw (ex-info "Not implemented" {})))

(defn count-by-criteria [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestMovieRepository []
  MovieRepository
  (save! [this movie] (save! this movie))
  (find-by-id [this id] (find-by-id this id))
  (find-by-uuid [this uuid] (find-by-uuid this uuid))
  (find-by-criteria [this criteria] (find-by-criteria this criteria))
  (count-by-criteria [this criteria] (count-by-criteria this criteria))) 