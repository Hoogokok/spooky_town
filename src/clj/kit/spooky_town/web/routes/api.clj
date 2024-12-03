(ns kit.spooky-town.web.routes.api
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [kit.spooky-town.web.controllers.health :as health]
   [kit.spooky-town.web.controllers.password :as password]
   [kit.spooky-town.web.controllers.role-request :as role-request]
   [kit.spooky-town.web.controllers.user :as user]
   [kit.spooky-town.web.middleware.auth :as auth]
   [kit.spooky-town.web.middleware.exception :as exception]
   [kit.spooky-town.web.middleware.formats :as formats]
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
                exception/wrap-exception
                  ;; authentication
                auth/wrap-auth-required]})

;; Routes
(defn api-routes [{:keys [role-request-use-case user-use-case tx-manager] :as opts}]
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

    ["/auth"
     ["/login"
      {:post {:handler (fn [req]
                         (user/authenticate
                          (assoc req :user-use-case user-use-case)))
              :parameters {:body {:email string?
                                  :password string?}}
              :responses {200 {:body {:token string?}}
                          400 {:body {:error string?}}
                          401 {:body {:error string?}}
                          403 {:body {:error string?}}
                          404 {:body {:error string?}}
                          500 {:body {:error string?}}}
              :summary "사용자 인증"
              :description "이메일과 비밀번호로 사용자를 인증합니다."
              :swagger {:tags ["auth"]}}}]]

    ["/users"
     ["" {:post {:handler (fn [req]
                            (user/register
                             (assoc req :user-use-case user-use-case)))
                 :parameters {:body {:email string?
                                     :name string?
                                     :password string?}}
                 :responses {201 {:body {:token string?}}
                             400 {:body {:error string?}}
                             409 {:body {:error string?}}
                             500 {:body {:error string?}}}
                 :summary "새로운 사용자를 등록합니다"
                 :description "이메일, 이름, 비밀번호로 새로운 사용자를 등록합니다."
                 :swagger {:tags ["users"]}}}]
     ["/:id/role"
      {:put {:handler (fn [req]
                        (user/update-user-role
                         (-> req
                             (assoc :user-use-case user-use-case)
                             (assoc-in [:body-params :user-uuid] (get-in req [:path-params :id])))))
             :parameters {:path {:id string?}
                          :body {:role string?}}
             :responses {200 {:body {:user-uuid string?
                                     :roles #{string?}}}
                         400 {:body {:error string?}}
                         404 {:body {:error string?}}
                         500 {:body {:error string?}}}
             :summary "사용자 역할 업데이트"
             :description "사용자의 역할을 업데이트합니다."
             :swagger {:tags ["users"]
                       :security [{:bearer []}]}}}]
     ["/me"
      ["/withdraw"
       {:delete {:handler (fn [req]
                            (user/withdraw
                             (assoc req :user-use-case user-use-case)))
                 :parameters {:body {:password string?
                                     :reason (s/nilable string?)}}
                 :responses {204 {}
                             400 {:body {:error string?}}
                             401 {:body {:error string?}}
                             404 {:body {:error string?}}
                             500 {:body {:error string?}}}
                 :summary "회원 탈퇴"
                 :description "비밀번호 확인 후 회원 탈퇴를 진행합니다."
                 :swagger {:tags ["users"]
                           :security [{:bearer []}]}}}]]]
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
                      :security [{:bearer []}]}}}

     ["/:id/approve"
      {:put {:handler (fn [req]
                        (role-request/approve-request
                         (assoc req :role-request-use-case role-request-use-case)))
             :parameters {:path {:id int?}}
             :responses {200 {:body {:message string?}}
                         400 {:body {:error string?}}
                         403 {:body {:error string?}}}}}]

     ["/:id/reject"
      {:put {:handler (fn [req]
                        (role-request/reject-request
                         (assoc req :role-request-use-case role-request-use-case)))
             :parameters {:path {:id int?}
                          :body {:reason string?}}
             :responses {200 {:body {:message string?}}
                         400 {:body {:error string?}}
                         403 {:body {:error string?}}}
             :summary "역할 변경 요청 거절"
             :description "관리자가 대기 중인 역할 변경 요청을 거절합니다."
             :swagger {:tags ["role-requests"]
                       :security [{:bearer []}]}}}]


     ["/users/password"
      ["/reset"
       {:post {:handler (fn [req]
                          (password/request-reset
                           (assoc req :user-use-case user-use-case)))
               :parameters {:body {:email string?}}
               :responses {200 {:body {:token string?}}
                           400 {:body {:error string?}}}
               :summary "비밀번호 초기화 요청"
               :description "이메일을 통해 비밀번호 초기화를 요청합니다."
               :swagger {:tags ["users"]}}}]

      ["/reset/confirm"
       {:post {:handler (fn [req]
                          (password/reset-password
                           (assoc req :user-use-case user-use-case)))
               :parameters {:body {:token string?
                                   :new-password string?}}
               :responses {200 {:body {:success boolean?}}
                           400 {:body {:error string?}}}
               :summary "비밀번호 초기화 완료"
               :description "토큰을 사용하여 새로운 비밀번호로 변경합니다."
               :swagger {:tags ["users"]}}}]]
     ["/admin"
      ["/users"
       ["/:id"
        {:delete {:handler (fn [req]
                             (user/delete-user
                              (assoc req :user-use-case user-use-case)))
                  :parameters {:path {:id string?}
                               :body {:reason string?}}
                  :responses {204 {}
                              400 {:body {:error string?}}
                              403 {:body {:error string?}}
                              404 {:body {:error string?}}
                              500 {:body {:error string?}}}
                  :summary "관리자가 사용자를 탈퇴 처리합니다"
                  :description "관리자 권한으로 특정 사용자를 탈퇴 처리합니다."
                  :swagger {:tags ["admin"]
                            :security [{:bearer []}]}}}]]]]]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn []
    (api-routes opts)))
