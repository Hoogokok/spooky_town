(ns kit.spooky-town.infrastructure.persistence.role-request
  (:require [kit.spooky-town.domain.role-request.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [integrant.core :as ig]))

(def role-requests-table :role_requests)

;; Query helpers
(defn- insert-role-request-query [request]
  (-> (h/insert-into role-requests-table)
      (h/values [{:uuid (:uuid request)
                  :user_id (:user-id request)
                  :requested_role (name (:requested-role request))
                  :reason (:reason request)
                  :status (name (:status request))}])
      (sql/format)))

(defn- find-by-id-query [id]
  (-> (h/select :*)
      (h/from role-requests-table)
      (h/where [:= :id id])
      (sql/format)))

(defn- find-by-uuid-query [uuid]
  (-> (h/select :*)
      (h/from role-requests-table)
      (h/where [:= :uuid uuid])
      (sql/format)))

(defn- find-all-by-user-query [user-id]
  (-> (h/select :*)
      (h/from role-requests-table)
      (h/where [:= :user_id user-id])
      (h/order-by [:created_at :desc])
      (sql/format)))

(defn- find-all-pending-query []
  (-> (h/select :*)
      (h/from role-requests-table)
      (h/where [:= :status "pending"])
      (h/order-by [:created_at :asc])
      (sql/format)))

(defn- update-role-request-query [request]
  (-> (h/update role-requests-table)
      (h/set {:status (name (:status request))
              :updated_at (java.util.Date.)
              :approved_by (:approved-by request)
              :rejected_by (:rejected-by request)
              :rejection_reason (:rejection-reason request)})
      (h/where [:= :id (:id request)])
      (sql/format)))

(defrecord RoleRequestRepository [datasource]
  UpdateQueryFn
  (update-query-fn [this query-fn]
    (assoc this :query-fn query-fn))

  protocol/RoleRequestRepository
  (save [this request]
    (let [[sql & params] (insert-role-request-query request)
          execute-fn (or (:query-fn this)
                        (fn [sql params opts]
                          (jdbc/execute-one! datasource sql params opts)))]
      (execute-fn sql params {:builder-fn rs/as-unqualified-kebab-maps})))

  (find-by-id [this id]
    (let [[sql & params] (find-by-id-query id)
          execute-fn (or (:query-fn this)
                        (fn [sql params opts]
                          (jdbc/execute-one! datasource sql params opts)))]
      (execute-fn sql params {:builder-fn rs/as-unqualified-kebab-maps})))

  (find-by-uuid [this uuid]
    (let [[sql & params] (find-by-uuid-query uuid)
          execute-fn (or (:query-fn this)
                        (fn [sql params opts]
                          (jdbc/execute-one! datasource sql params opts)))]
      (execute-fn sql params {:builder-fn rs/as-unqualified-kebab-maps})))

  (find-all-by-user [this user-id]
    (let [[sql & params] (find-all-by-user-query user-id)
          execute-fn (or (:query-fn this)
                        (fn [sql params opts]
                          (jdbc/execute! datasource sql params opts)))]
      (execute-fn sql params {:builder-fn rs/as-unqualified-kebab-maps})))

  (find-all-pending [this]
    (let [[sql & params] (find-all-pending-query)
          execute-fn (or (:query-fn this)
                        (fn [sql params opts]
                          (jdbc/execute! datasource sql params opts)))]
      (execute-fn sql params {:builder-fn rs/as-unqualified-kebab-maps})))

  (update-request [this request]
    (let [[sql & params] (update-role-request-query request)
          execute-fn (or (:query-fn this)
                        (fn [sql params opts]
                          (jdbc/execute-one! datasource sql params opts)))]
      (execute-fn sql params {:builder-fn rs/as-unqualified-kebab-maps}))))

(defmethod ig/init-key :infrastructure/role-request-repository [_ {:keys [datasource]}]
  (->RoleRequestRepository datasource)) 