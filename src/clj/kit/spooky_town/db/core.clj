 (ns kit.spooky-town.db.core
   (:require
    [conman.core :as conman]
    [kit.edge.db.sql.conman :as db]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log]))
 
 (defn with-transaction*
   "트랜잭션 내에서 함수를 실행합니다.
    opts는 다음과 같은 옵션을 지원합니다:
    :isolation - 트랜잭션 격리 수준 (:read-committed, :repeatable-read, :serializable)
    :read-only? - 읽기 전용 트랜잭션 여부"
   [db f & [{:keys [isolation read-only?]
             :or {isolation :read-committed
                  read-only? false}}]]
   (try
     (conman/with-transaction
       [db {:isolation isolation
            :read-only? read-only?}]
       (f))
     (catch Exception e
       (log/error e "Transaction failed")
       (throw e))))
 
 (defmacro with-transaction
   "트랜잭션 매크로
    (with-transaction db
      {:isolation :serializable}
      (do-something)
      (do-another-thing))"
   [db opts & body]
   `(with-transaction* ~db (fn [] ~@body) ~opts))
 
 ;; 일반적으로 사용되는 트랜잭션 래퍼들
 (defn with-read-only
   "읽기 전용 트랜잭션에서 함수를 실행합니다."
   [db f]
   (with-transaction* db f {:read-only? true}))
 
 (defn with-serializable
   "직렬화 가능한 트랜잭션에서 함수를 실행합니다."
   [db f]
   (with-transaction* db f {:isolation :serializable}))