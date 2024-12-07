(ns kit.spooky-town.infrastructure.persistence.role
  (:require [kit.spooky-town.domain.role.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [TransactionManager]]
            [integrant.core :as ig]))

(defrecord RoleRepository [datasource tx-manager queries]
  protocol/RoleRepository
  (save! [this role]
    (.with-tx tx-manager
              (fn [tx-query-fn]
                (tx-query-fn (:save-role! queries) datasource role))))

  (find-by-id [this role-id]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-role-by-id queries) datasource {:role-id role-id}))))

  (find-by-name [this role-name]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-role-by-name queries) datasource {:role-name (name role-name)}))))

  (find-all [this]
    (.with-read-only tx-manager
                     (fn [tx-query-fn]
                       (tx-query-fn (:get-all-roles queries) datasource {})))))

(defmethod ig/init-key :infrastructure/role-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->RoleRepository datasource tx-manager queries)) 