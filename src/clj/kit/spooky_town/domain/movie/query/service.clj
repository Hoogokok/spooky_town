(ns kit.spooky-town.domain.movie.query.service
  (:require 
            [kit.spooky-town.domain.movie.entity :as entity]
            [kit.spooky-town.domain.movie.repository.protocol :as movie-repo]
            [kit.spooky-town.domain.movie-actor.repository.protocol :as movie-actor-repo]
            [kit.spooky-town.domain.movie-director.repository.protocol :as movie-director-repo] 
            [integrant.core :as ig]))

(defprotocol MovieQueryService
  (find-movie [this params]
    "영화 상세 정보를 조회합니다. 
     params: {:movie-id string?
              :include-actors boolean?
              :include-directors boolean?}")

  (search-movies [this params]
    "영화 목록을 검색합니다.
     params: {:page pos-int?
              :sort-by keyword?
              :sort-order keyword?
              :title string?
              :director-name string?
              :actor-name string?
              :genres set?
              :release-status keyword?}")

  (get-movie-summary [this params]
    "영화 요약 정보를 조회합니다.
     params: {:movie-id string?}"))

(defrecord MovieQueryServiceImpl [movie-repository movie-actor-repository movie-director-repository tx-manager]
  MovieQueryService
  (find-movie [_ params]
    (.with-read-only tx-manager
      (fn [_]
        (when-let [movie (movie-repo/find-by-id movie-repository (:movie-id params))]
          (cond-> movie
            (:include-actors params)
            (assoc :actors (movie-actor-repo/find-actors-by-movie movie-actor-repository (:movie-id params)))
            
            (:include-directors params)
            (assoc :directors (movie-director-repo/find-directors-by-movie movie-director-repository (:movie-id params))))))))

  (search-movies [_ params]
    (.with-read-only tx-manager
      (fn [_]
        (let [movies (movie-repo/find-by-criteria movie-repository params)
              total-count (movie-repo/count-by-criteria movie-repository params)]
          {:movies (mapv entity/->summary movies)
           :page (:page params)
           :total-count total-count
           :total-pages (int (Math/ceil (/ total-count 12.0)))}))))

  (get-movie-summary [_ params]
    (.with-read-only tx-manager
      (fn [_]
        (when-let [movie (movie-repo/find-by-id movie-repository (:movie-id params))]
          (entity/->summary movie))))))

(defmethod ig/init-key :domain/movie-query-service
  [_ {:keys [movie-repository movie-actor-repository movie-director-repository tx-manager]}]
  (->MovieQueryServiceImpl movie-repository movie-actor-repository movie-director-repository tx-manager))