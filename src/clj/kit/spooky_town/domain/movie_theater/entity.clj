(ns kit.spooky-town.domain.movie-theater.entity
  (:require [clojure.spec.alpha :as s]
            [kit.spooky-town.domain.movie-theater.value :as value]))

;; 엔티티 스펙
(s/def ::movie-theater
  (s/keys :req-un [::value/movie-id
                   ::value/theater-id
                   ::value/created-at]))

;; 엔티티 레코드
(defrecord MovieTheater [movie-id theater-id created-at])

;; 생성 함수
(defn create-movie-theater [{:keys [movie-id theater-id created-at]
                            :or {created-at (value/create-timestamp)}}]
  (let [movie-theater (map->MovieTheater
                       {:movie-id movie-id
                        :theater-id theater-id
                        :created-at created-at})]
    (when (s/valid? ::movie-theater movie-theater)
      movie-theater)))

;; 유효성 검사
(defn valid? [movie-theater]
  (s/valid? ::movie-theater movie-theater)) 