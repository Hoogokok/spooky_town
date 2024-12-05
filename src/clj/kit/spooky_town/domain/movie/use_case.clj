(ns kit.spooky-town.domain.movie.use-case
  (:require [integrant.core :as ig]
            [failjure.core :as f]
            [kit.spooky-town.domain.movie.entity :as entity]
            [kit.spooky-town.domain.movie.value :as value]
            [kit.spooky-town.domain.movie.repository.protocol :as movie-repository]
            [kit.spooky-town.domain.director.repository.protocol :as director-repository]
            [kit.spooky-town.domain.actor.repository.protocol :as actor-repository]
            [kit.spooky-town.domain.common.image.gateway.protocol :as image-gateway]))

(defprotocol MovieUseCase
  (create-movie [this command]
    "새로운 영화를 생성합니다."))

(defrecord CreateMovieCommand 
  [title 
   director-names     ;; ["봉준호" "박찬욱"]
   release-info      ;; {:release-status :upcoming :release-date "2024-12-25"}
   genres            ;; #{:horror :psychological}
   actor-infos       ;; [{:actor-name "송강호" :role "형사"} {:actor-name "배두나" :role "기자"}]
   runtime           ;; 120
   poster-file])     ;; {:tempfile #object[java.io.File...] :filename "poster.jpg" :content-type "image/jpeg"}

(defrecord CreateMovieUseCaseImpl [with-tx 
                                  movie-repository 
                                  director-repository 
                                  actor-repository 
                                  image-gateway 
                                  id-generator]
  MovieUseCase
  (create-movie [_ command]
    ...))

(defmethod ig/init-key ::create-movie-use-case 
  [_ {:keys [with-tx 
            movie-repository 
            director-repository 
            actor-repository 
            image-gateway 
            id-generator]}]
  (->CreateMovieUseCaseImpl with-tx 
                           movie-repository 
                           director-repository 
                           actor-repository 
                           image-gateway 
                           id-generator))
