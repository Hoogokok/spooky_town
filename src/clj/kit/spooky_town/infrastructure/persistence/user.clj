(ns kit.spooky-town.infrastructure.persistence.user
  (:require [kit.spooky-town.domain.user.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [UpdateQueryFn]]
            [integrant.core :as ig]
            [next.jdbc.result-set :as rs]))

(defrecord UserRepository [datasource tx-manager queries]
  UpdateQueryFn
  (update-query-fn [this tx-fn]
    (assoc this :query-fn tx-fn))

  protocol/UserRepository
  (save! [this user]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:save-user! queries)
                   {:uuid (:uuid user)
                    :email (:email user)
                    :password_hash (:hashed-password user)
                    :name (:name user)}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-id [this id]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-user-by-id queries)
                   {:id id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-email [this email]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-user-by-email queries)
                   {:email email}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-by-uuid [this uuid]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:get-user-by-uuid queries)
                   {:uuid uuid}
                   {:builder-fn rs/as-unqualified-maps})))))

  (delete! [this id]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:delete-user! queries)
                   {:id id}
                   {:builder-fn rs/as-unqualified-maps})))))

  (find-id-by-uuid [this uuid]
    (.with-read-only tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (:id (query-fn (:find-user-id-by-uuid queries)
                        {:uuid uuid}
                        {:builder-fn rs/as-unqualified-maps}))))))
  
  (mark-as-withdrawn [this user]
    (.with-tx tx-manager [this]
      (fn [repo]
        (let [query-fn (:query-fn repo)]
          (query-fn (:mark-user-as-withdrawn queries)
                   user
                   {:builder-fn rs/as-unqualified-maps}))))))

(defmethod ig/init-key :infrastructure/user-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->UserRepository datasource tx-manager queries)) 