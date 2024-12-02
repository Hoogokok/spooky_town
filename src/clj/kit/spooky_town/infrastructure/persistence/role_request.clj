(ns kit.spooky-town.infrastructure.persistence.role-request
  (:require [kit.spooky-town.domain.role-request.repository.protocol :as protocol] 
            [integrant.core :as ig]))

(defrecord RoleRequestRepository [datasource tx-manager queries]
  protocol/RoleRequestRepository
  (save [this request]
    (let [query-fn (fn [query-params]
                     ((:insert-role-request! queries) datasource query-params))]
      (.with-tx tx-manager
        (fn [tx-query-fn]
          (tx-query-fn {:uuid (:uuid request)
                        :user_id (:user-id request)
                        :requested_role (name (:requested-role request))
                        :reason (:reason request)
                        :status (name (:status request))})))))

  (find-by-id [this id]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:find-role-request-by-id queries) datasource {:id id}))))

  (find-by-uuid [this uuid]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:find-role-request-by-uuid queries) datasource {:uuid uuid}))))

  (find-all-by-user [this user-id]
    ((:find-all-role-requests-by-user queries) datasource {:user_id user-id}))

  (find-all-pending [this]
    ((:find-all-pending-role-requests queries) datasource))

  (update-request [this request]
    (let [query-fn (fn [query-params]
                     ((:update-role-request! queries) datasource query-params))]
      (.with-tx tx-manager
        (fn [tx-query-fn]
          (tx-query-fn request))))))

(defmethod ig/init-key :infrastructure/role-request-repository 
  [_ {:keys [datasource tx-manager queries]}]
  (->RoleRequestRepository datasource tx-manager queries)) 