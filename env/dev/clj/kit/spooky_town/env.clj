(ns kit.spooky-town.env
  (:require
    [clojure.tools.logging :as log]
    [kit.spooky-town.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[spooky_town starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[spooky_town started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[spooky_town has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
