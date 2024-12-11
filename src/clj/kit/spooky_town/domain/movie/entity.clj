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
(s/def ::is-deleted boolean?)
(s/def ::deleted-at (s/nilable inst?))

(s/def ::movie
  (s/keys :req-un [::movie-id ::uuid ::created-at ::updated-at
                   ::title ::release-info ::genres ::is-deleted]
  :opt-un [::runtime ::poster ::deleted-at]))

(defrecord Movie [movie-id uuid title release-info genres
                  runtime poster
                  created-at updated-at
  is-deleted deleted-at])

(defn create-movie
  [{:keys [movie-id uuid title release-info genres
           runtime poster
           created-at updated-at
   is-deleted deleted-at]
  :or {is-deleted false}}]
  (let [movie (map->Movie
               (cond-> {:movie-id movie-id
                       :uuid uuid
:title title
:release-info release-info
:genres genres
                       :created-at created-at
 :updated-at (or updated-at created-at)
 :is-deleted is-deleted}

                 runtime
                 (assoc :runtime runtime)

                 poster
                 (assoc :poster poster)

   deleted-at
   (assoc :deleted-at deleted-at)))]
    (when (s/valid? ::movie movie)
      movie)))

;; 조회용 요약 정보 변환
(defn ->summary [{:keys [movie-uuid title poster  genres
                         release-info] :as movie}]
  (when (and movie-uuid title release-info
             genres)
    {:movie-uuid movie-uuid
     :title title
     :poster-url (:url poster) 
     :genres genres
     :director-names (when (:directors movie)
                       (mapv :name (:directors movie))) 
     :release-info release-info}))

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

(defn mark-as-deleted
  [movie deleted-at]
  (when (and (not (:is-deleted movie)) deleted-at)
    (-> movie
        (assoc :is-deleted true
               :deleted-at deleted-at
               :updated-at deleted-at))))

(defn deleted?
  [movie]
  (:is-deleted movie))

(defn update-movie [movie {:keys [title runtime genres release-info poster]} updated-at]
  (when-not (deleted? movie)
    (when-let [updated (-> movie
                           (cond->
                            title (update-title title)
                            runtime (update-runtime runtime)
                            genres (update-genres genres)
                            release-info (update-release-info release-info)
                            poster (update-poster poster))
                          (assoc :updated-at updated-at))]
   (create-movie updated))))
