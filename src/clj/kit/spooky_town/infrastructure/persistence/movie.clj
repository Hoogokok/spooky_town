(ns kit.spooky-town.infrastructure.persistence.movie
  (:require [kit.spooky-town.domain.movie.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord MovieRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/MovieRepository
  (save! [this movie]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-movie! queries)
                   movie
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-id [this id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-movie-by-id queries)
                   {:id id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-uuid [this uuid]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-movie-by-uuid queries)
                   {:uuid uuid}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/movie-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->MovieRepository datasource tx-manager queries)) 