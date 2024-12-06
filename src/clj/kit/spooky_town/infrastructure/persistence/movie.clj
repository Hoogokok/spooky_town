(ns kit.spooky-town.infrastructure.persistence.movie
  (:require [kit.spooky-town.domain.movie.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [TransactionManager]]
            [integrant.core :as ig]))

(defrecord MovieRepository [datasource tx-manager queries]
  protocol/MovieRepository
  (save! [this movie]
    (.with-tx tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:save-movie! queries) datasource movie))))

  (find-by-id [this id]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-movie-by-id queries) datasource {:id id}))))

  (find-by-uuid [this uuid]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-movie-by-uuid queries) datasource {:uuid uuid})))))

(defmethod ig/init-key :infrastructure/movie-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->MovieRepository datasource tx-manager queries)) 