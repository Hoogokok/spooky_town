(ns kit.spooky-town.infrastructure.persistence.role-request
  (:require [kit.spooky-town.domain.role-request.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [integrant.core :as ig]))

(def role-requests-table :role_requests)

(defn- insert-role-request-query [request]
  (-> (h/insert-into role-requests-table)
      (h/values [{:uuid (:uuid request)
                  :user_id (:user-id request)
                  :requested_role (name (:requested-role request))
                  :reason (:reason request)
                  :status (name (:status request))}])
      (sql/format)))

(defrecord RoleRequestRepository [datasource]
  UpdateQueryFn
  (update-query-fn [this query-fn]
    (assoc this :query-fn query-fn))

  protocol/RoleRequestRepository
  (save [this request]
    (let [[sql & params] (insert-role-request-query request)
          execute-fn (or (:query-fn this)  ; 트랜잭션 컨텍스트가 있으면 사용
                        (fn [sql params opts]  ; 없으면 기본 실행
                          (jdbc/execute-one! datasource sql params opts)))]
      (execute-fn sql params {:builder-fn rs/as-unqualified-kebab-maps}))))

(defmethod ig/init-key :infrastructure/role-request-repository [_ {:keys [datasource]}]
  (->RoleRequestRepository datasource)) 