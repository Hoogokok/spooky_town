(ns kit.spooky-town.infrastructure.persistence.movie-actor
  (:require [kit.spooky-town.domain.movie-actor.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [TransactionManager]]
            [integrant.core :as ig]))

(defrecord MovieActorRepository [datasource tx-manager queries]
  protocol/MovieActorRepository
  (save! [this movie-actor]
    (.with-tx tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:save-movie-actor! queries) datasource movie-actor))))

  (find-by-movie-id [this movie-id]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-actors-by-movie-id queries) datasource {:movie_id movie-id}))))

  (find-by-actor-id [this actor-id]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-movies-by-actor-id queries) datasource {:actor_id actor-id})))))

(defmethod ig/init-key :infrastructure/movie-actor-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->MovieActorRepository datasource tx-manager queries)) 