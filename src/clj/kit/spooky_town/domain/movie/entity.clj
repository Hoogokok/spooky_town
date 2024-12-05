(ns kit.spooky-town.domain.movie.entity
  (:require [clojure.spec.alpha :as s]
            [kit.spooky-town.domain.movie.value :as value]
            [kit.spooky-town.domain.common.image :as image]))

;; 엔티티 스펙 - 값 객체 스펙 사용
(s/def ::movie-id ::value/movie-id)
(s/def ::uuid uuid?)
(s/def ::created-at ::value/created-at)
(s/def ::updated-at ::value/updated-at)
(s/def ::title ::value/title)
(s/def ::director-ids ::value/director-ids)
(s/def ::release-info ::value/release-info)
(s/def ::genres ::value/genres)
(s/def ::runtime (s/nilable ::value/runtime))
(s/def ::movie-actors (s/nilable ::value/movie-actors))
(s/def ::poster (s/nilable ::image/image))

(s/def ::movie
  (s/keys :req-un [::movie-id ::uuid ::created-at ::updated-at
                   ::title ::director-ids
                   ::release-info ::genres]
          :opt-un [::runtime ::movie-actors ::poster]))

(defn explain-movie
  "영화 엔티티의 유효성 검증 실패 이유를 반환합니다."
  [movie]
  (when-let [ed (s/explain-data ::movie movie)]
    {:problems (::s/problems ed)
     :spec (::s/spec ed)
     :value (::s/value ed)}))

(defrecord Movie [movie-id uuid title director-ids release-info genres
                  runtime movie-actors poster
                  created-at updated-at])

(defn create-movie
  [{:keys [movie-id uuid title director-ids release-info genres
           runtime movie-actors poster
           created-at updated-at]}]
  (let [movie (map->Movie
               (cond-> {:movie-id movie-id  
                        :uuid uuid
                        :title title
                        :director-ids director-ids  
                        :release-info release-info
                        :genres genres
                        :created-at (or created-at (java.util.Date.))
                        :updated-at (or updated-at (java.util.Date.))
                        :movie-actors movie-actors} 

                 runtime
                 (assoc :runtime runtime)

                 poster
                 (assoc :poster poster)))]
    (if (s/valid? ::movie movie)
      movie
      (let [validation-error (explain-movie movie)]
        (println "Movie validation failed:")
        (println "Problems:" (-> validation-error :problems))
        nil))))

