(ns kit.spooky-town.infrastructure.persistence.actor
  (:require [kit.spooky-town.domain.actor.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [TransactionManager]]
            [integrant.core :as ig]))

(defrecord ActorRepository [datasource tx-manager queries]
  protocol/ActorRepository
  (save! [this actor]
    (.with-tx tx-manager
              (fn [tx-query-fn]
                (tx-query-fn (:save-actor! queries) datasource actor))))

  (find-by-id [this id]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-actor-by-id queries) datasource {:actor_id id}))))

  (find-by-name [this name]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-actor-by-name queries) datasource {:name name})))))

(defmethod ig/init-key :infrastructure/actor-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->ActorRepository datasource tx-manager queries)) 