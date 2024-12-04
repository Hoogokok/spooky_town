(ns kit.spooky-town.web.routes.role-request
  (:require [kit.spooky-town.web.controllers.role-request :as role-request]))

(defn role-request-routes [{:keys [role-request-use-case]}]
  ["/role-requests"
   ["" 
    {:post {:handler (fn [req]
                      (role-request/create-request
                       (assoc req :role-request-use-case role-request-use-case)))
            :parameters {:body [:map
                              [:role :string]
                              [:reason :string]]}
            :responses {201 {:body [:map [:message :string]]}
                       400 {:body [:map [:error :string]]}}
            :summary "역할 변경 요청 생성"
            :description "사용자가 새로운 역할로 변경을 요청합니다. 관리자 승인이 필요합니다."
            :swagger {:tags ["role-requests"]
                     :security [{:bearer []}]}}

     :get {:handler (fn [req]
                     (role-request/get-pending-requests
                      (assoc req :role-request-use-case role-request-use-case)))
           :responses {200 {:body [:vector
                                 [:map
                                  [:uuid :string]
                                  [:user_id :int]
                                  [:requested_role :string]
                                  [:reason :string]
                                  [:status :string]
                                  [:created_at :string]]]}
                      403 {:body [:map [:error :string]]}}
           :summary "대기 중인 역할 변경 요청 목록 조회"
           :description "관리자가 승인 대기 중인 역할 변경 요청 목록을 조회합니다."
           :swagger {:tags ["role-requests"]
                    :security [{:bearer []}]}}}]

   ["/:id/approve"
    {:put {:handler (fn [req]
                     (role-request/approve-request
                      (assoc req :role-request-use-case role-request-use-case)))
           :parameters {:path [:map [:id :int]]}
           :responses {200 {:body [:map [:message :string]]}
                      400 {:body [:map [:error :string]]}
                      403 {:body [:map [:error :string]]}}
           :summary "역할 변경 요청 승인"
           :description "관리자가 대기 중인 역할 변경 요청을 승인합니다."
           :swagger {:tags ["role-requests"]
                    :security [{:bearer []}]}}}]

   ["/:id/reject"
    {:put {:handler (fn [req]
                     (role-request/reject-request
                      (assoc req :role-request-use-case role-request-use-case)))
           :parameters {:path [:map [:id :int]]
                       :body [:map [:reason :string]]}
           :responses {200 {:body [:map [:message :string]]}
                      400 {:body [:map [:error :string]]}
                      403 {:body [:map [:error :string]]}}
           :summary "역할 변경 요청 거절"
           :description "관리자가 대기 중인 역할 변경 요청을 거절합니다."
           :swagger {:tags ["role-requests"]
                    :security [{:bearer []}]}}}]]) 