(ns kit.spooky-town.web.controllers.health
  (:require
   [kit.spooky-town.web.responses :as responses]
   [kit.spooky-town.infrastructure.persistence.transaction :as tx])
  (:import
   [java.util Date]))

(defn healthcheck!
  [{:keys [tx-manager] :as req}]
  (try
    (let [db-status (try
                      (tx/with-read-only
                        tx-manager
                        (fn [_] (= 1 1)))
                      true
                      (catch Exception _ false))]
      (responses/ok
       {:time     (str (Date. (System/currentTimeMillis)))
        :up-since (str (Date. (.getStartTime (java.lang.management.ManagementFactory/getRuntimeMXBean))))
        :app      {:status  "up"
                   :message ""}
        :db       {:status  (if db-status "up" "down")
                   :message (if db-status "" "Database connection failed")}}))
    (catch Exception e
      (responses/ok
       {:time     (str (Date. (System/currentTimeMillis)))
        :up-since (str (Date. (.getStartTime (java.lang.management.ManagementFactory/getRuntimeMXBean))))
        :app      {:status  "up"
                   :message ""}
        :db       {:status  "down"
                   :message (.getMessage e)}}))))