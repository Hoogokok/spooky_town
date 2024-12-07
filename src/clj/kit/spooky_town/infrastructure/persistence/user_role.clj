(ns kit.spooky-town.infrastructure.persistence.user-role
  (:require [kit.spooky-town.domain.user-role.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [TransactionManager]]
            [integrant.core :as ig]))

(defrecord UserRoleRepository [datasource tx-manager queries]
  protocol/UserRoleRepository
  (add-user-role! [this user-id role-id]
    (.with-tx tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:add-user-role! queries) datasource {:user-id user-id :role-id role-id}))))

  (remove-user-role! [this user-id role-id]
    (.with-tx tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:remove-user-role! queries) datasource {:user-id user-id :role-id role-id}))))

  (find-roles-by-user [this user-id]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-user-roles queries) datasource {:user-id user-id}))))

  (find-users-by-role [this role-id]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-role-users queries) datasource {:role-id role-id})))))

(defmethod ig/init-key :infrastructure/user-role-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->UserRoleRepository datasource tx-manager queries)) 