(ns kit.spooky-town.infrastructure.persistence.movie-theater
  (:require [kit.spooky-town.domain.movie-theater.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord MovieTheaterRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/MovieTheaterRepository
  (save-movie-theater! [this movie-id theater-id]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-movie-theater! queries)
                   {:movie_id movie-id
                    :theater_id theater-id
                    :created_at (java.util.Date.)}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-theaters-by-movie [this movie-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-theaters-by-movie queries)
                   {:movie_id movie-id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-theaters-by-movies [this movie-ids]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-theaters-by-movies queries)
                   {:movie_ids movie-ids}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-movies-by-theater [this theater-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-movies-by-theater queries)
                   {:theater_id theater-id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (delete-movie-theater! [this movie-id theater-id]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:delete-movie-theater! queries)
                   {:movie_id movie-id
                    :theater_id theater-id}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/movie-theater-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->MovieTheaterRepository datasource tx-manager queries)) 