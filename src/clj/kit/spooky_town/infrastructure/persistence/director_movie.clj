(ns kit.spooky-town.infrastructure.persistence.director-movie
  (:require [kit.spooky-town.domain.director-movie.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [TransactionManager]]
            [integrant.core :as ig]))

(defrecord DirectorMovieRepository [datasource tx-manager queries]
  protocol/DirectorMovieRepository
  (save! [this director-movie]
    (.with-tx tx-manager
              (fn [tx-query-fn]
                (tx-query-fn (:save-director-movie! queries) datasource director-movie))))

  (find-by-director-id [this director-id]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-movies-by-director-id queries) datasource {:director_id director-id}))))

  (find-by-movie-id [this movie-id]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-directors-by-movie-id queries) datasource {:movie_id movie-id})))))

(defmethod ig/init-key :infrastructure/director-movie-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->DirectorMovieRepository datasource tx-manager queries)) 