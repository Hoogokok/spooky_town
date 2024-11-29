(ns kit.spooky-town.web.controllers.health
  (:require
    [kit.spooky-town.web.responses :as responses]
    [kit.edge.db.sql.conman :as db])
  (:import
    [java.util Date]))

(defn healthcheck!
  [{:keys [db] :as req}]
  (try
    (let [db-status (db/health-check db)]
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
