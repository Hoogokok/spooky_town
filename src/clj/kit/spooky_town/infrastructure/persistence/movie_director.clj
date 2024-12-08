(ns kit.spooky-town.infrastructure.persistence.movie-director
  (:require [kit.spooky-town.domain.movie-director.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord MovieDirectorRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/MovieDirectorRepository
  (save-movie-director! [this movie-id director-id role]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-director-movie! queries)
                   {:movie_id movie-id
                    :director_id director-id
                    :role role}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-directors-by-movie [this movie-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-directors-by-movie-id queries)
                   {:movie_id movie-id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-movies-by-director [this director-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-movies-by-director-id queries)
                   {:director_id director-id}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/movie-director-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->MovieDirectorRepository datasource tx-manager queries)) 