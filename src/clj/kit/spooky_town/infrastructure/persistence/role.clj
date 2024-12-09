(ns kit.spooky-town.infrastructure.persistence.role
  (:require [kit.spooky-town.domain.role.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord RoleRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/RoleRepository
  (save! [this role]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-role! queries)
                   role
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-id [this role-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-role-by-id queries)
                   {:role-id role-id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-name [this role-name]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-role-by-name queries)
                   {:role-name (name role-name)}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-all [this]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-all-roles queries)
                   {}
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/role-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->RoleRepository datasource tx-manager queries)) 