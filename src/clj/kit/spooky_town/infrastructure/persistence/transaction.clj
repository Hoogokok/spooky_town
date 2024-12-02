(ns kit.spooky-town.infrastructure.persistence.transaction
  (:require
   [next.jdbc :as jdbc]
   [clojure.tools.logging :as log]
   [integrant.core :as ig]))

(defprotocol TransactionManager
  (with-transaction* [this f opts])
  (with-read-only [this f]))

(defrecord DefaultTransactionManager [conn]
  TransactionManager
  (with-transaction* [_ f {:keys [isolation read-only?]
                          :or {isolation :read-committed
                               read-only? false}}]
    (try
      (log/debug "Starting transaction with options:" 
                 {:isolation isolation :read-only? read-only?})
      (jdbc/with-transaction [tx conn {:isolation-level isolation
                                     :read-only? read-only?}]
        (let [result (f tx)]
          (log/debug "Transaction completed successfully")
          result))
      (catch Exception e
        (log/error e "Transaction failed with error:" 
                   {:isolation isolation 
                    :read-only? read-only?
                    :error (.getMessage e)})
        (throw e))))
  
  (with-read-only [this f]
    (with-transaction* this f {:read-only? true})))

(defmethod ig/init-key :db/tx-manager [_ {:keys [conn]}]
  (->DefaultTransactionManager conn)) 