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
   [kit.spooky-town.web.controllers.role-request :as role-request]
   [kit.spooky-town.web.middleware.auth :as auth] 
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
                exception/wrap-exception
                  ;; authentication
                auth/wrap-auth-required]})

;; Routes
(defn api-routes [{:keys [role-request-use-case tx-manager] :as opts}]
  ["/api"
   route-data
   ["/v1"
    ["/swagger.json"
     {:get {:no-doc true
            :swagger {:info {:title "kit.spooky-town API v1"
                             :description "API for managing horror/thriller content"
                             :version "1.0.0"}}
            :handler (swagger/create-swagger-handler)}}]
    ["/health"
     {:get {:handler (fn [req]
                       (health/healthcheck! (assoc req :tx-manager tx-manager)))}}]
    
    ["/role-requests"
     {:post {:handler (fn [req]
                       (role-request/create-request 
                         (assoc req :role-request-use-case role-request-use-case)))
             :parameters {:body {:role string?
                               :reason string?}}
             :responses {201 {:body {:message string?}}
                        400 {:body {:error string?}}}
             :summary "역할 변경 요청 생성"
             :description "사용자가 새로운 역할로 변경을 요청합니다. 관리자 승인이 필요합니다."
             :swagger {:tags ["role-requests"]
                      :security [{:bearer []}]}}
      
      :get {:handler (fn [req]
                      (role-request/get-pending-requests
                        (assoc req :role-request-use-case role-request-use-case)))
            :responses {200 {:body [{:uuid string?
                                   :user_id int?
                                   :requested_role string?
                                   :reason string?
                                   :status string?
                                   :created_at string?}]}
                       403 {:body {:error string?}}}
            :summary "대기 중인 역할 변경 요청 목록 조회"
            :description "관리자가 승인 대기 중인 역할 변경 요청 목록을 조회합니다."
            :swagger {:tags ["role-requests"]
                     :security [{:bearer []}]}}}]]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn []
    (api-routes opts)))
