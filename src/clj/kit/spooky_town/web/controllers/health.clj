(ns kit.spooky-town.web.controllers.health
  (:require
   [kit.spooky-town.web.responses :as responses])
  (:import
   [java.util Date]))

(defn healthcheck!
  [{:keys [datasource] :as req}]
  (try
    (let [db-status (try
                     (with-open [conn (.getConnection datasource)]
                       (with-open [stmt (.createStatement conn)]
                         (.execute stmt "SELECT 1")))
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