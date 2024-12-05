(ns kit.spooky-town.domain.movie-actor.test.repository
  (:require [kit.spooky-town.domain.movie-actor.repository.protocol :refer [MovieActorRepository]]))

(defn save-movie-actor! [_ _ _ _]
  (throw (ex-info "Not implemented" {})))

(defn find-actors-by-movie [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-movies-by-actor [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestMovieActorRepository []
  MovieActorRepository
  (save-movie-actor! [this movie-id actor-id role] 
    (save-movie-actor! this movie-id actor-id role))
  (find-actors-by-movie [this movie-id] 
    (find-actors-by-movie this movie-id))
  (find-movies-by-actor [this actor-id] 
    (find-movies-by-actor this actor-id))) 