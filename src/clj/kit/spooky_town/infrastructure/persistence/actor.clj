(ns kit.spooky-town.infrastructure.persistence.actor
  (:require [kit.spooky-town.domain.actor.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord ActorRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/ActorRepository
  (save! [this actor]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-actor! queries)
                   actor
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-id [this id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-actor-by-id queries)
                   {:id id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-name [this name]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-actor-by-name queries)
                   {:name name}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/actor-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->ActorRepository datasource tx-manager queries)) 