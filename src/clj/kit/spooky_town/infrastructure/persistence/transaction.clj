(ns kit.spooky-town.infrastructure.persistence.transaction
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]))

(defprotocol UpdateQueryFn
  "Repository의 쿼리 함수를 트랜잭션 컨텍스트로 업데이트하기 위한 프로토콜"
  (update-query-fn [this tx-fn]
    "트랜잭션 컨텍스트의 쿼리 함수로 repository를 업데이트합니다."))

(defprotocol TransactionManager
  (with-tx [this repositories f]
    "여러 repository에 대해 트랜잭션을 실행합니다.")
  (with-read-only [this repositories f]
    "여러 repository에 대해 읽기 전용 트랜잭션을 실행합니다."))

(defrecord PgTransactionManager [conn]
  TransactionManager
  (with-tx [_ repositories f]
    (jdbc/with-transaction [tx conn {:isolation :read-committed}]
      (let [tx-fn (fn [sql params opts]
                    (jdbc/execute-one! tx (into [sql] params) opts))
            updated-repos (map #(update-query-fn % tx-fn) repositories)]
        (apply f updated-repos))))
  
  (with-read-only [_ repositories f]
    (jdbc/with-transaction [tx conn {:isolation :read-committed
                                    :read-only true}]
      (let [tx-fn (fn [sql params opts]
                    (jdbc/execute-one! tx (into [sql] params) opts))
            updated-repos (map #(update-query-fn % tx-fn) repositories)]
        (apply f updated-repos)))))

(defmethod ig/init-key :db/tx-manager [_ {:keys [conn]}]
  (->PgTransactionManager conn)) 