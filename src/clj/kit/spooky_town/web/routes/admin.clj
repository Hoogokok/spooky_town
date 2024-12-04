(ns kit.spooky-town.web.routes.admin
  (:require [kit.spooky-town.web.controllers.user :as user]
            [kit.spooky-town.web.routes.common :refer [authenticated-route-data]]))

(defn admin-routes [{:keys [user-use-case]}]
  ["/admin"
   (merge authenticated-route-data
          {"/users"
           ["/:id"
            {:delete {:handler (fn [req]
                               (user/delete-user
                                (assoc req :user-use-case user-use-case)))
                     :parameters {:path [:map [:id :string]]
                                :body [:map [:reason :string]]}
                     :responses {204 {}
                               400 {:body [:map [:error :string]]}
                               403 {:body [:map [:error :string]]}
                               404 {:body [:map [:error :string]]}
                               500 {:body [:map [:error :string]]}}
                     :summary "관리자가 사용자를 탈퇴 처리합니다"
                     :description "관리자 권한으로 특정 사용자를 탈퇴 처리합니다."
                     :swagger {:tags ["admin"]
                             :security [{:bearer []}]}}}]})]) 