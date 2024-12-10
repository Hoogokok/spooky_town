(ns kit.spooky-town.domain.movie-theater.test.repository
  (:require [kit.spooky-town.domain.movie-theater.repository.protocol :refer [MovieTheaterRepository]]))

(defn save-movie-theater! [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defn find-theaters-by-movie [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-theaters-by-movies [_ _]
  (throw (ex-info "Not implemented" {})))

(defn find-movies-by-theater [_ _]
  (throw (ex-info "Not implemented" {})))

(defn delete-movie-theater! [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestMovieTheaterRepository []
  MovieTheaterRepository
  (save-movie-theater! [this movie-id theater-id]
    (save-movie-theater! this movie-id theater-id))
  
  (find-theaters-by-movie [this movie-id]
    (find-theaters-by-movie this movie-id))
  
  (find-theaters-by-movies [this movie-ids]
    (find-theaters-by-movies this movie-ids))
  
  (find-movies-by-theater [this theater-id]
    (find-movies-by-theater this theater-id))
  
  (delete-movie-theater! [this movie-id theater-id]
    (delete-movie-theater! this movie-id theater-id))) 