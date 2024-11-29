(ns kit.spooky-town.db.core
  (:require
   [integrant.core :as ig]
   [hugsql.core :as hugsql]
   [clojure.tools.logging :as log]))

(defmethod ig/init-key :db.sql/queries [_ {:keys [conn queries options]}]
  (try
    (log/info "Initializing database queries from" queries)
    (reduce (fn [acc query-file]
              (merge acc (hugsql/map-of-db-fns query-file {:connection conn
                                                          :options options})))
            {}
            queries)
    (catch Exception e
      (log/error e "Failed to initialize database queries")
      (throw e)))) 