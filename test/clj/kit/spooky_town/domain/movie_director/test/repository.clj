(ns kit.spooky-town.domain.movie-director.test.repository
  (:require [kit.spooky-town.domain.movie-director.repository.protocol :refer [MovieDirectorRepository]]))

(defn save-movie-director! [_ _ _ _]
  (throw (ex-info "Not implemented" {})))

(defn find-directors-by-movie [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-movies-by-director [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestMovieDirectorRepository []
  MovieDirectorRepository
  (save-movie-director! [this movie-id director-id role] 
    (save-movie-director! this movie-id director-id role))
  (find-directors-by-movie [this movie-id] 
    (find-directors-by-movie this movie-id))
  (find-movies-by-director [this director-id] 
    (find-movies-by-director this director-id))) 