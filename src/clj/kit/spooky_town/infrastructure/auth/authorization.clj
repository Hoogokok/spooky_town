(ns kit.spooky-town.infrastructure.auth.authorization
  (:require [kit.spooky-town.domain.auth.authorization.protocol :refer [UserAuthorization]]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord UserAuthorizationImpl [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  UserAuthorization
  (has-permission? [this user-uuid permission]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)
              roles (query-fn (:get-user-roles queries)
                            {:user_uuid user-uuid}
                            {:builder-fn rs/as-unqualified-maps})]
          (contains? (set (map :role roles)) permission))))))

(defmethod ig/init-key :infrastructure/user-authorization
  [_ {:keys [datasource tx-manager queries]}]
  (->UserAuthorizationImpl datasource tx-manager queries)) 