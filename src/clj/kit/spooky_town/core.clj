(ns kit.spooky-town.core
  (:require
   [clojure.tools.logging :as log]
   [integrant.core :as ig]
   [kit.spooky-town.config :as config]
   [kit.spooky-town.env :refer [defaults]]

    ;; Edges       
   [kit.edge.server.undertow]
   [kit.spooky-town.web.handler]
   [kit.edge.db.sql.conman]
   [kit.edge.db.sql.migratus]

   ;; Routes
   [kit.spooky-town.web.routes.api]
   [kit.spooky-town.web.middleware.auth]
   [kit.spooky-town.web.middleware.multipart]

   ;; Infrastructure
   [kit.spooky-town.infrastructure.email.smtp-gateway]
   [kit.spooky-town.infrastructure.auth.jwt-email-token-gateway]
   [kit.spooky-town.infrastructure.auth.bcrypt-gateway]
   [kit.spooky-town.infrastructure.auth.email-verification-gateway]
   [kit.spooky-town.infrastructure.auth.authorization]
   [kit.spooky-town.infrastructure.id.ulid]
   [kit.spooky-town.infrastructure.id.uuid-generator]
   [kit.spooky-town.infrastructure.image.cloudinary]
   ;; Database
   [kit.spooky-town.infrastructure.persistence.core]
   [kit.spooky-town.infrastructure.event.memory]
   [kit.spooky-town.infrastructure.persistence.role-request]
   [kit.spooky-town.infrastructure.persistence.user]
   [kit.spooky-town.infrastructure.persistence.movie]
   [kit.spooky-town.infrastructure.persistence.actor]
   [kit.spooky-town.infrastructure.persistence.director]
   [kit.spooky-town.infrastructure.persistence.movie-director]
   [kit.spooky-town.infrastructure.persistence.movie-actor]
   [kit.spooky-town.infrastructure.persistence.movie-theater]
   [kit.spooky-town.infrastructure.persistence.theater]
   [kit.spooky-town.infrastructure.persistence.role]
   [kit.spooky-town.infrastructure.persistence.user-role]
   ;; Use-cases
   [kit.spooky-town.domain.role-request.use-case]
   [kit.spooky-town.domain.user.use-case]
   [kit.spooky-town.domain.movie.use-case]
   ;; Query Services
   [kit.spooky-town.domain.movie.query.service])

   (:gen-class))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (fn [thread ex]
      (log/error {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on" (.getName thread))})))

(defonce system (atom nil))

(defn stop-app []
  ((or (:stop defaults) (fn [])))
  (some-> (deref system) (ig/halt!)))

(defn start-app [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/expand)
       (ig/init)
       (reset! system)))

(defn -main [& _]
  (start-app)
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app))
  (.addShutdownHook (Runtime/getRuntime) (Thread. shutdown-agents)))
