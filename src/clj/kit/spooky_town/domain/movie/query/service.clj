(ns kit.spooky-town.domain.movie.query.service
  (:require 
            [kit.spooky-town.domain.movie.entity :as entity]
            [kit.spooky-town.domain.movie.repository.protocol :as movie-repo]
            [kit.spooky-town.domain.movie-actor.repository.protocol :as movie-actor-repo]
            [kit.spooky-town.domain.movie-director.repository.protocol :as movie-director-repo] 
            [kit.spooky-town.domain.movie-theater.repository.protocol :as movie-theater-repo]
            [kit.spooky-town.domain.movie-provider.repository.protocol :as movie-provider-repo]
            [integrant.core :as ig]))

(defprotocol MovieQueryService
  (find-movie [this params]
    "영화 상세 정보를 조회합니다. 
     params: {:movie-uuid string?
              :include-actors boolean?
              :include-directors boolean?
              :include-theaters boolean?
              :include-providers boolean?}")

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
     params: {:movie-uuid string?}"))

(defrecord MovieQueryServiceImpl [movie-repository 
                                movie-actor-repository 
                                movie-director-repository 
                                movie-theater-repository
                                movie-provider-repository
                                with-read-only]
  MovieQueryService
  (find-movie [_ params]
    (with-read-only [movie-repository 
                     movie-actor-repository 
                     movie-director-repository 
                     movie-theater-repository
                     movie-provider-repository]
      (fn [movie-repo actor-repo director-repo theater-repo provider-repo]
        (when-let [movie-id (movie-repo/find-id-by-uuid movie-repo (:movie-uuid params))]
          (when-let [movie (movie-repo/find-by-id movie-repo movie-id)]
            (cond-> movie
              (:include-actors params)
              (assoc :actors (movie-actor-repo/find-actors-by-movie actor-repo movie-id))
              
              (:include-directors params)
              (assoc :directors (movie-director-repo/find-directors-by-movie director-repo movie-id))

              (:include-theaters params)
              (assoc :theaters (movie-theater-repo/find-theaters-by-movie theater-repo movie-id))
              
              (:include-providers params)
              (assoc :providers (movie-provider-repo/find-by-movie provider-repo movie-id))))))))

  (search-movies [_ params]
    (with-read-only [movie-repository]
      (fn [repo]
        (let [movies (movie-repo/find-by-criteria repo params)
              total-count (movie-repo/count-by-criteria repo params)]
          {:movies (mapv entity/->summary movies)
           :page (:page params)
           :total-count total-count
           :total-pages (int (Math/ceil (/ total-count 12.0)))}))))

  (get-movie-summary [_ params]
    (with-read-only [movie-repository]
      (fn [repo]
        (when-let [movie-id (movie-repo/find-id-by-uuid repo (:movie-uuid params))]
          (when-let [movie (movie-repo/find-by-id repo movie-id)]
            (entity/->summary movie)))))))

(defmethod ig/init-key :domain/movie-query-service
  [_ {:keys [movie-repository 
             movie-actor-repository 
             movie-director-repository 
             movie-theater-repository
             movie-provider-repository
             with-read-only]}]
  (->MovieQueryServiceImpl 
    movie-repository 
    movie-actor-repository 
    movie-director-repository 
    movie-theater-repository
    movie-provider-repository
    with-read-only))
