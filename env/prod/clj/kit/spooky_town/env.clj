(ns kit.spooky-town.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[spooky_town starting]=-"))
   :start      (fn []
                 (log/info "\n-=[spooky_town started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[spooky_town has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
