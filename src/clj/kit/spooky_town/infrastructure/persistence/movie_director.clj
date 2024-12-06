(ns kit.spooky-town.infrastructure.persistence.movie-director
  (:require [kit.spooky-town.domain.movie-director.repository.protocol :as protocol] 
            [integrant.core :as ig]))

(defrecord MovieDirectorRepository [datasource tx-manager queries]
  protocol/MovieDirectorRepository
  (save-movie-director! [this movie-id director-id role]
    (.with-tx tx-manager
              (fn [tx-query-fn]
                (tx-query-fn (:save-director-movie! queries) 
                            datasource 
                            {:movie_id movie-id
                             :director_id director-id
                             :role role}))))

  (find-directors-by-movie [this movie-id]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-directors-by-movie-id queries) datasource {:movie_id movie-id}))))

  (find-movies-by-director [this director-id]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-movies-by-director-id queries) datasource {:director_id director-id})))))

(defmethod ig/init-key :infrastructure/movie-director-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->MovieDirectorRepository datasource tx-manager queries)) 