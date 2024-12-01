(ns kit.spooky-town.web.middleware.error
  (:require [failjure.core :as f]
            [integrant.core :as ig]
            [clojure.tools.logging :as log]))

(defn error-response
  "에러 응답을 생성합니다."
  [{:keys [type message status]
    :or {status 500
         type :internal-server-error}}]
  {:status status
   :body {:error type
          :message message}})

(def error-handlers
  "에러 타입별 처리기"
  {:auth/unauthorized {:status 401
                      :type :unauthorized}
   :auth/token-expired {:status 401
                       :type :token-expired}
   :auth/invalid-token {:status 401
                       :type :invalid-token}})

(defn wrap-error-handling
  "전역 에러 처리 미들웨어"
  [handler]
  (fn [request]
    (try
      (let [response (handler request)]
        (if (f/failed? response)
          (let [{:keys [type message]} (f/message response)
                error-def (get error-handlers type)]
            (log/error :error-type type :message message)
            (error-response (assoc error-def :message message)))
          response))
      (catch Exception e
        (log/error e "Unhandled exception")
        (error-response {:message (.getMessage e)})))))

(defmethod ig/init-key :web.middleware.error/error [_ {:keys [error-handlers]}]
  {:name ::error
   :wrap #(wrap-error-handling %)})

(defmethod ig/init-key :web.middleware.error/handlers [_ {:keys [handlers]}]
  handlers) 