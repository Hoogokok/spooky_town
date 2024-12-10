(ns kit.spooky-town.domain.movie-theater.use-case
  (:require [kit.spooky-town.domain.movie-theater.entity :as entity]
            [kit.spooky-town.domain.movie-theater.repository.protocol :as movie-theater-repo]
            [integrant.core :as ig]))

(defprotocol MovieTheaterUseCase
  (assign-theater! [this command]
    "영화에 극장을 할당합니다.
     command: {:movie-id string?
               :theater-id string?}")

  (remove-theater! [this command]
    "영화에서 극장을 제거합니다.
     command: {:movie-id string?
               :theater-id string?}")

  (get-theaters [this command]
    "영화의 극장 목록을 조회합니다.
     command: {:movie-id string?}")

  (get-movies [this command]
    "극장의 영화 목록을 조회합니다.
     command: {:theater-id string?}"))

(defrecord MovieTheaterUseCaseImpl [movie-theater-repository with-tx]
  MovieTheaterUseCase
  (assign-theater! [_ command]
    (with-tx [movie-theater-repository]
      (fn [repo]
        (let [{:keys [movie-id theater-id]} command]
          (movie-theater-repo/save-movie-theater! repo movie-id theater-id)))))

  (remove-theater! [_ command]
    (with-tx [movie-theater-repository]
      (fn [repo]
        (let [{:keys [movie-id theater-id]} command]
          (movie-theater-repo/delete-movie-theater! repo movie-id theater-id)))))

  (get-theaters [_ command]
    (movie-theater-repo/find-theaters-by-movie movie-theater-repository (:movie-id command)))

  (get-movies [_ command]
    (movie-theater-repo/find-movies-by-theater movie-theater-repository (:theater-id command))))

(defmethod ig/init-key :domain/movie-theater-use-case
  [_ {:keys [movie-theater-repository with-tx]}]
  (->MovieTheaterUseCaseImpl movie-theater-repository with-tx)) 