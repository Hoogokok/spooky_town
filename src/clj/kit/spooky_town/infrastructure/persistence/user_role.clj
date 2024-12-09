(ns kit.spooky-town.infrastructure.persistence.user-role
  (:require [kit.spooky-town.domain.user-role.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord UserRoleRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/UserRoleRepository
  (add-user-role! [this user-id role-id]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:add-user-role! queries)
                   {:user-id user-id :role-id role-id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-roles-by-user [this user-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-user-roles queries)
                   {:user-id user-id}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/user-role-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->UserRoleRepository datasource tx-manager queries)) 