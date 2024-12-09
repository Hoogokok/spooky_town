(ns kit.spooky-town.infrastructure.persistence.theater
  (:require [kit.spooky-town.domain.theater.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord TheaterRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/TheaterRepository
  (save! [this theater]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-theater! queries)
                   theater
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-id [this theater-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-theater-by-id queries)
                   {:theater_id theater-id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-uuid [this uuid]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-theater-by-uuid queries)
                   {:uuid uuid}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-id-by-uuid [this uuid]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)
              result (query-fn (:get-theater-by-uuid queries)
                              {:uuid uuid}
                              {:builder-fn rs/as-unqualified-maps})]
          (:theater_id result)))))

  (find-by-chain-type [this chain-type]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:find-theaters-by-chain-type queries)
                   {:chain_type (name chain-type)}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/theater-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->TheaterRepository datasource tx-manager queries)) 