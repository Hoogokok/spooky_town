(ns kit.spooky-town.infrastructure.persistence.transaction
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]))

(defprotocol UpdateQueryFn
  "Repository의 쿼리 함수를 트랜잭션 컨텍스트로 업데이트하기 위한 프로토콜"
  (update-query-fn [this query-fn]))

(defprotocol TransactionManager
  (with-tx [this f])
  (with-read-only [this f]))

(defrecord PgTransactionManager [conn]
  TransactionManager
  (with-tx [this f]
    (jdbc/with-transaction [tx conn {:isolation :read-committed}]
      (let [query-fn (fn [sql params opts]
                       (jdbc/execute-one! tx (into [sql] params) opts))]
        (f query-fn))))
  
  (with-read-only [this f]
    (jdbc/with-transaction [tx conn {:isolation :read-committed
                                    :read-only true}]
      (f tx))))

(defmethod ig/init-key :db/tx-manager [_ {:keys [conn]}]
  (->PgTransactionManager conn)) 