(ns kit.spooky-town.infrastructure.persistence.role-request
  (:require [kit.spooky-town.domain.role-request.repository.protocol :as protocol] 
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord RoleRequestRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/RoleRequestRepository
  (save! [this request]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:insert-role-request! queries)
                   {:uuid (:uuid request)
                    :user_id (:user-id request)
                    :requested_role (name (:requested-role request))
                    :reason (:reason request)
                    :status (name (:status request))}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-id [this id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:find-role-request-by-id queries)
                   {:id id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-uuid [this uuid]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:find-role-request-by-uuid queries)
                   {:uuid uuid}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-id-by-uuid [this uuid]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (:id (query-fn (:find-role-request-id-by-uuid queries)
                        {:uuid uuid}
                        {:builder-fn rs/as-unqualified-maps}))))))

  (find-all-by-user [this user-id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:find-all-role-requests-by-user queries)
                   {:user_id user-id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-all-pending [this]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:find-all-pending-role-requests queries)
                   {}
                   {:builder-fn rs/as-unqualified-maps})))))

  (update-request [this request]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:update-role-request! queries)
                   request
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/role-request-repository 
  [_ {:keys [datasource tx-manager queries]}]
  (->RoleRequestRepository datasource tx-manager queries)) 