(ns kit.spooky-town.web.routes.user
  (:require [kit.spooky-town.web.controllers.user :as user]
            [kit.spooky-town.web.routes.common :refer [authenticated-route-data]]))

(defn user-routes [{:keys [user-use-case]}]
  ["/users"
   ["/:id/role"  ;; 사용자 역할 업데이트
    (merge authenticated-route-data
           {:put {:handler (fn [req]
                           (user/update-user-role
                            (-> req
                                (assoc :user-use-case user-use-case)
                                (assoc-in [:body-params :user-uuid] 
                                        (get-in req [:path-params :id])))))
                  :parameters {:path [:map 
                                   [:id :string]]
                             :body [:map 
                                   [:role :string]]}
                  :responses {200 {:body [:map 
                                       [:user-uuid :string]
                                       [:roles [:set :string]]]}
                            400 {:body [:map [:error :string]]}
                            404 {:body [:map [:error :string]]}
                            500 {:body [:map [:error :string]]}}
                  :summary "사용자 역할 업데이트"
                  :description "사용자의 역할을 업데이트합니다."
                  :swagger {:tags ["users"]
                          :security [{:bearer []}]}}})]
   
   ["/me"  ;; 회원 탈퇴
    ["/withdraw"
     (merge authenticated-route-data
            {:delete {:handler (fn [req]
                               (user/withdraw
                                (assoc req :user-use-case user-use-case)))
                     :parameters {:body [:map
                                      [:password :string]
                                      [:reason {:optional true} :string]]}
                     :responses {204 {}
                               400 {:body [:map [:error :string]]}
                               401 {:body [:map [:error :string]]}
                               404 {:body [:map [:error :string]]}
                               500 {:body [:map [:error :string]]}}
                     :summary "회원 탈퇴"
                     :description "비밀번호 확인 후 회원 탈퇴를 진행합니다."
                     :swagger {:tags ["users"]
                             :security [{:bearer []}]}}})]]])
 