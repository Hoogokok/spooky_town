(ns kit.spooky-town.web.routes.api
  (:require
   [integrant.core :as ig]
   [kit.spooky-town.web.middleware.auth :as auth]
   [kit.spooky-town.web.middleware.exception :as exception]
   [kit.spooky-town.web.middleware.formats :as formats]
   [kit.spooky-town.web.routes.health :refer [health-routes]]
   [kit.spooky-town.web.routes.auth :refer [auth-routes]]
   [kit.spooky-town.web.routes.user :refer [user-routes]]
   [kit.spooky-town.web.routes.role-request :refer [role-request-routes]]
   [kit.spooky-town.web.routes.admin :refer [admin-routes]]
   [reitit.coercion.malli :as malli]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats/instance
   :swagger    {:id ::api}
   :middleware [parameters/parameters-middleware
                muuntaja/format-negotiate-middleware
                muuntaja/format-response-middleware
                coercion/coerce-exceptions-middleware
                muuntaja/format-request-middleware
                coercion/coerce-response-middleware
                coercion/coerce-request-middleware
                exception/wrap-exception]})

(def authenticated-route-data
  {:middleware [auth/wrap-auth-required]})

(defn api-routes [opts]
  ["/api"
   route-data
   ["/v1"
    ["/swagger.json"
     {:get {:no-doc true
            :swagger {:info {:title "kit.spooky-town API v1"
                           :description "API for managing horror/thriller content"
                           :version "1.0.0"}}
            :handler (swagger/create-swagger-handler)}}]
    (health-routes opts)
    (auth-routes opts)
    (user-routes opts)
    (role-request-routes opts)
    (admin-routes opts)]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn []
    (api-routes opts)))
