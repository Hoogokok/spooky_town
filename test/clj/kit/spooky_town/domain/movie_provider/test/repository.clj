(ns kit.spooky-town.domain.movie-provider.test.repository
  (:require [kit.spooky-town.domain.movie-provider.repository.protocol :refer [MovieProviderRepository]]))

(defn save! [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-id [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-uuid [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-movie [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-by-provider [_ _]
  (throw (ex-info "Not implemented" {})))

(defn delete! [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestMovieProviderRepository []
  MovieProviderRepository
  (save! [this movie-provider] (save! this movie-provider))
  (find-by-id [this id] (find-by-id this id))
  (find-by-uuid [this uuid] (find-by-uuid this uuid))
  (find-by-movie [this movie-id] (find-by-movie this movie-id))
  (find-by-provider [this provider-id] (find-by-provider this provider-id))
  (delete! [this movie-id provider-id] (delete! this movie-id provider-id))) 