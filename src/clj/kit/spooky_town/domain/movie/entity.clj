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
(s/def ::release-info ::value/release-info)
(s/def ::genres ::value/genres)
(s/def ::runtime (s/nilable ::value/runtime))
(s/def ::poster (s/nilable ::image/image))

(s/def ::movie
  (s/keys :req-un [::movie-id ::uuid ::created-at ::updated-at
                   ::title ::release-info ::genres]
          :opt-un [::runtime ::poster]))

(defrecord Movie [movie-id uuid title release-info genres
                  runtime poster
                  created-at updated-at])

(defn create-movie
  [{:keys [movie-id uuid title release-info genres
           runtime poster
           created-at updated-at]}]
  (let [movie (map->Movie
               (cond-> {:movie-id movie-id
                        :uuid uuid
                        :title title
                        :release-info release-info
                        :genres genres
                        :created-at (or created-at (java.util.Date.))
                        :updated-at (or updated-at (java.util.Date.))}

                 runtime
                 (assoc :runtime runtime)

                 poster
                 (assoc :poster poster)))]
    (when (s/valid? ::movie movie)
      movie)))

;; 조회용 요약 정보 변환
(defn ->summary [{:keys [movie-id title poster release-date genres
                         release-status] :as movie}]
  (when (and movie-id title poster release-date
             genres release-status)
    {:movie-id movie-id
     :title title
     :poster-url (:url poster)
     :release-date release-date
     :genres genres
     :director-names (when (:directors movie)
                      (mapv :name (:directors movie)))
     :release-status release-status}))

(defn- update-title [movie new-title]
  (when-let [validated-title (value/create-title new-title)]
    (assoc movie :title validated-title)))

(defn- update-runtime [movie new-runtime]
  (when-let [validated-runtime (value/create-runtime new-runtime)]
    (assoc movie :runtime validated-runtime)))

(defn- update-genres [movie new-genres]
  (when-let [validated-genres (value/create-genres new-genres)]
    (assoc movie :genres validated-genres)))

(defn- update-release-info [movie new-release-info]
  (when-let [validated-release-info (value/create-release-info new-release-info)]
    (assoc movie :release-info validated-release-info)))

(defn- update-poster [movie new-poster]
  (when-let [validated-poster (value/create-poster new-poster)]
    (assoc movie :poster validated-poster)))

(defn update-movie [movie {:keys [title runtime genres release-info poster]}]
  (when-let [updated (-> movie
                        (cond-> 
                          title (update-title title)
                          runtime (update-runtime runtime)
                          genres (update-genres genres)
                          release-info (update-release-info release-info)
                          poster (update-poster poster))
                        (assoc :updated-at (java.util.Date.)))]
    (create-movie updated)))
