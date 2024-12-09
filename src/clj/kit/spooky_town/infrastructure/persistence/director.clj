(ns kit.spooky-town.infrastructure.persistence.director
  (:require [kit.spooky-town.domain.director.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord DirectorRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/DirectorRepository
  (save! [this director]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-director! queries)
                   director
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-id [this id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-director-by-id queries)
                   {:id id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-name [this name]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-director-by-name queries)
                   {:name name}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/director-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->DirectorRepository datasource tx-manager queries)) 