(ns kit.spooky-town.domain.movie.entity
  (:require [clojure.spec.alpha :as s]
            [kit.spooky-town.domain.movie.value :as value]
            [kit.spooky-town.domain.common.image :as image]))

;; 식별성 관련 스펙
(s/def ::uuid uuid?)
(s/def ::created-at inst?)
(s/def ::updated-at inst?)

;; 필수 속성 스펙 (값 객체 활용)
(s/def ::title ::value/title)
(s/def ::director-ids ::value/director-ids)
(s/def ::release-info ::value/release-info)
(s/def ::genres ::value/genres)

;; 선택 속성 스펙 (값 객체 활용)
(s/def ::movie-actors ::value/movie-actors)
(s/def ::runtime ::value/runtime)
(s/def ::poster ::image/image)

;; 영화 엔티티 스펙
(s/def ::movie
  (s/keys :req-un [::uuid ::created-at ::updated-at
                   ::title ::director-ids ::release-info ::genres]
          :opt-un [::movie-actors ::runtime ::poster]))

;; 생성 함수
(defn create-movie [{:keys [uuid title director-ids release-info genres] :as movie}]
  (let [created-title (value/create-title title)
        created-director-ids (value/create-director-ids director-ids)
        created-release-info (value/create-release-info release-info)
        created-genres (value/create-genres genres)]
    (when (and uuid created-title created-director-ids created-release-info created-genres)
      (let [now (java.util.Date.)]
        {:uuid uuid
         :created-at now
         :updated-at now
         :title title
         :director-ids director-ids
         :release-info release-info
         :genres genres})))) 