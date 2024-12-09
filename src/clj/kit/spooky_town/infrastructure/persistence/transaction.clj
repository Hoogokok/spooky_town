(ns kit.spooky-town.infrastructure.persistence.transaction
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]))

(def ^:private default-isolation-level :read-committed)
(def ^:private default-tx-opts
  {:isolation default-isolation-level})
(def ^:private default-read-only-tx-opts
  (assoc default-tx-opts :read-only true))

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
    (try
      (jdbc/with-transaction [tx conn default-tx-opts]
        (let [tx-fn (fn [sql params opts]
                     (jdbc/execute! tx (into [sql] params) opts))
              updated-repos (mapv #(update-query-fn % tx-fn) repositories)]
          (apply f updated-repos)))
      (catch Exception e
        (throw (ex-info "Transaction failed"
                       {:type :transaction/failed
                        :cause (.getMessage e)}
                       e)))))
  
  (with-read-only [_ repositories f]
    (try
      (jdbc/with-transaction [tx conn default-read-only-tx-opts]
        (let [tx-fn (fn [sql params opts]
                     (jdbc/execute! tx (into [sql] params) opts))
              updated-repos (mapv #(update-query-fn % tx-fn) repositories)]
          (apply f updated-repos)))
      (catch Exception e
        (throw (ex-info "Read-only transaction failed"
                       {:type :transaction/failed
                        :cause (.getMessage e)}
                       e))))))

(defmethod ig/init-key :db/tx-manager [_ {:keys [conn]}]
  (->PgTransactionManager conn)) 