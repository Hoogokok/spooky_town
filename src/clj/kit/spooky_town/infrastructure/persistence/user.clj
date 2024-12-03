(ns kit.spooky-town.infrastructure.persistence.user
  (:require [kit.spooky-town.domain.user.repository.protocol :as protocol]
            [kit.spooky-town.infrastructure.persistence.transaction :refer [TransactionManager]]
            [integrant.core :as ig]))

(defrecord UserRepository [datasource tx-manager queries]
  protocol/UserRepository
  (save! [this user]
    (.with-tx tx-manager
      (fn [tx-query-fn]
        (tx-query-fn {:uuid (:uuid user)
                      :email (:email user)
                      :password_hash (:hashed-password user)
                      :name (:name user)}))))

  (find-by-id [this id]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-user-by-id queries) datasource {:id id}))))

  (find-by-email [this email]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-user-by-email queries) datasource {:email email}))))

  (find-by-uuid [this uuid]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:get-user-by-uuid queries) datasource {:uuid uuid}))))

  (delete! [this id]
    (.with-tx tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:delete-user! queries) datasource {:id id}))))

  (find-id-by-uuid [this uuid]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (:id (tx-query-fn (:get-user-id-by-uuid queries) datasource {:uuid uuid}))))))

(defmethod ig/init-key :infrastructure/user-repository
  [_ {:keys [datasource tx-manager queries]}]
  (->UserRepository datasource tx-manager queries)) 