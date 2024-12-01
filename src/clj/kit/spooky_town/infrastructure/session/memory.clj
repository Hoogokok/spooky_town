(ns kit.spooky-town.infrastructure.session.memory
  (:require [kit.spooky-town.infrastructure.session.repository :as repository]
            [integrant.core :as ig]
            [clojure.tools.logging :as log])
  (:import [java.util UUID]))

(defrecord InMemorySessionStore [sessions]
  repository/SessionRepository
  (create-session [_ user-data]
    (let [session-id (str (UUID/randomUUID))
          session {:id session-id
                  :user user-data
                  :created-at (System/currentTimeMillis)
                  :expires-at (+ (System/currentTimeMillis) (* 24 60 60 1000))}]  ;; 24시간
      (swap! sessions assoc session-id session)
      (log/debug :session-created {:session-id session-id})
      session-id))
  
  (get-session [_ session-id]
    (when-let [session (get @sessions session-id)]
      (when (> (:expires-at session) (System/currentTimeMillis))
        (:user session))))
  
  (delete-session [_ session-id]
    (swap! sessions dissoc session-id)
    (log/debug :session-deleted {:session-id session-id})
    true)
  
  (cleanup-expired-sessions [_]
    (let [now (System/currentTimeMillis)
          expired (filter (fn [[_ session]]
                           (< (:expires-at session) now))
                         @sessions)]
      (doseq [[session-id _] expired]
        (swap! sessions dissoc session-id))
      (log/debug :expired-sessions-cleaned {:count (count expired)}))))

(defmethod ig/init-key :infrastructure.session/memory [_ _]
  (->InMemorySessionStore (atom {}))) 