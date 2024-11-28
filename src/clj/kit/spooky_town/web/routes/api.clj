(ns kit.spooky-town.web.routes.api
  (:require
    [kit.spooky-town.web.controllers.health :as health]
    [kit.spooky-town.web.middleware.exception :as exception]
    [kit.spooky-town.web.middleware.formats :as formats]
    [integrant.core :as ig]
    [reitit.coercion.malli :as malli]
    [reitit.ring.coercion :as coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.swagger :as swagger]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats/instance
   :swagger    {:id ::api}
   :middleware [;; query-params & form-params
                parameters/parameters-middleware
                  ;; content-negotiation
                muuntaja/format-negotiate-middleware
                  ;; encoding response body
                muuntaja/format-response-middleware
                  ;; exception handling
                coercion/coerce-exceptions-middleware
                  ;; decoding request body
                muuntaja/format-request-middleware
                  ;; coercing response bodys
                coercion/coerce-response-middleware
                  ;; coercing request parameters
                coercion/coerce-request-middleware
                  ;; exception handling
                exception/wrap-exception]})

;; Routes
(defn api-routes [_opts]
  [["/api/v1"
    {:swagger {:id ::api-v1}}
    ["/swagger.json"
     {:get {:no-doc true
            :swagger {:info {:title "kit.spooky-town API v1"
                           :description "API for managing horror/thriller content"
                           :version "1.0.0"}}
            :handler (swagger/create-swagger-handler)}}]
    ["/health"
     {:get health/healthcheck!}]]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn [] [base-path route-data (api-routes opts)]))
