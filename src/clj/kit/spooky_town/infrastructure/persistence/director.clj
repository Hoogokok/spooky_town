(ns kit.spooky-town.infrastructure.persistence.director
  (:require [kit.spooky-town.domain.director.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [TransactionManager]]
            [integrant.core :as ig]))

(defrecord DirectorRepository [datasource tx-manager queries]
  protocol/DirectorRepository
  (save! [this director]
    (.with-tx tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:save-director! queries) datasource director))))

  (find-by-id [this id]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-director-by-id queries) datasource {:id id}))))

  (find-by-name [this name]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-director-by-name queries) datasource {:name name})))))

(defmethod ig/init-key :infrastructure/director-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->DirectorRepository datasource tx-manager queries)) 