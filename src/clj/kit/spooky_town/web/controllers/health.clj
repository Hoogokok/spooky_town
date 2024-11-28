(ns kit.spooky-town.web.controllers.health
  (:require
    [kit.spooky-town.web.responses :as responses])
  (:import
    [java.util Date]))

(defn healthcheck!
  [req]
  (responses/ok
    {:time     (str (Date. (System/currentTimeMillis)))
     :up-since (str (Date. (.getStartTime (java.lang.management.ManagementFactory/getRuntimeMXBean))))
     :app      {:status  "up"
                :message ""}}))
