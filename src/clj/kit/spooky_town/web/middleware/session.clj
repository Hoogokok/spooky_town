(ns kit.spooky-town.web.middleware.session
  (:require [kit.spooky-town.infrastructure.session.repository :as repository]
            [integrant.core :as ig]
            [clojure.tools.logging :as log]))

(defn- get-session-id
  "요청에서 세션 ID를 추출합니다."
  [request cookie-name]
  (get-in request [:cookies cookie-name :value]))

(defn- create-cookie
  "세션 쿠키를 생성합니다."
  [cookie-name value cookie-attrs]
  (assoc-in {} [:cookies cookie-name] 
            (merge {:value value} cookie-attrs)))

(defn- handle-expired-session
  "만료된 세션을 처리합니다."
  [handler request session-store session-id cookie-name cookie-attrs]
  (log/debug :session-expired {:session-id session-id})
  (repository/delete-session session-store session-id)
  (-> (handler (assoc request :session nil))
      (merge (create-cookie cookie-name "" (assoc cookie-attrs :max-age 0)))))

(defn- handle-active-session
  "유효한 세션을 처리합니다."
  [handler request session session-id cookie-name cookie-attrs]
  (let [response (handler (assoc request :session session))]
    (merge response (create-cookie cookie-name session-id cookie-attrs))))

(defn wrap-session
  "세션 처리 미들웨어"
  [handler {:keys [session-store cookie-name cookie-attrs]}]
  (fn [request]
    (if-let [session-id (get-session-id request cookie-name)]
      (if-let [session (repository/get-session session-store session-id)]
        (handle-active-session handler request session session-id cookie-name cookie-attrs)
        (handle-expired-session handler request session-store session-id cookie-name cookie-attrs))
      (handler (assoc request :session nil)))))

(defmethod ig/init-key :web.middleware/session [_ config]
  {:name ::session
   :wrap #(wrap-session % config)})