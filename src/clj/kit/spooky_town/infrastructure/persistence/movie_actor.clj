(ns kit.spooky-town.infrastructure.persistence.movie-actor
  (:require [kit.spooky-town.domain.movie-actor.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord MovieActorRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/MovieActorRepository
  (save-movie-actor! [this movie-id actor-id role]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-movie-actor! queries)
                   {:movie_id movie-id :actor_id actor-id :role role}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-actors-by-movie [this movie-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-actors-by-movie-id queries)
                   {:movie_id movie-id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-movies-by-actor [this actor-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-movies-by-actor-id queries)
                   {:actor_id actor-id}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/movie-actor-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->MovieActorRepository datasource tx-manager queries)) 