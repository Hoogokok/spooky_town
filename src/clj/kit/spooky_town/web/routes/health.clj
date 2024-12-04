(ns kit.spooky-town.web.routes.health
  (:require [kit.spooky-town.web.controllers.health :as health]))

(defn health-routes [opts]
  ["/health"
   {:get {:handler (fn [req]
                    (health/healthcheck! (assoc req :tx-manager (:tx-manager opts))))}}]) 