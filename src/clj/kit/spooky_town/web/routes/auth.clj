(ns kit.spooky-town.web.routes.auth
  (:require [kit.spooky-town.web.controllers.user :as user]
            [kit.spooky-town.web.controllers.password :as password]))

(defn auth-routes [{:keys [user-use-case]}]
  ["/auth"
   ["/login"
    {:post {:handler (fn [req]
                       (user/authenticate
                        (assoc req :user-use-case user-use-case)))
            :parameters {:body [:map
                                [:email :string]
                                [:password :string]]}
            :responses {200 {:body [:map [:token :string]]}
                        400 {:body [:map [:error :string]]}
                        401 {:body [:map [:error :string]]}
                        403 {:body [:map [:error :string]]}
                        404 {:body [:map [:error :string]]}
                        500 {:body [:map [:error :string]]}}
            :summary "사용자 인증"
            :description "이메일과 비밀번호로 사용자를 인증합니다."
            :swagger {:tags ["auth"]}}}]

   ["/register"
    [""
     {:post {:handler (fn [req]
                        (user/register
                         (assoc req :user-use-case user-use-case)))
             :parameters {:body [:map
                                 [:email :string]
                                 [:name :string]
                                 [:password :string]]}
             :responses {201 {:body [:map [:token :string]]}
                         400 {:body [:map [:error :string]]}
                         409 {:body [:map [:error :string]]}
                         500 {:body [:map [:error :string]]}}
             :summary "새로운 사용자를 등록합니다"
             :description "이메일, 이름, 비밀번호로 새로운 사용자를 등록합니다."
             :swagger {:tags ["auth"]}}}]

    ["/verify"
     {:post {:handler (fn [req]
                        (user/request-email-verification
                         (assoc req :user-use-case user-use-case)))
             :parameters {:body [:map
                                 [:email :string]]}
             :responses {200 {:body [:map [:message :string]]}
                         400 {:body [:map [:error :string]]}
                         404 {:body [:map [:error :string]]}
                         429 {:body [:map [:error :string]]}}
             :summary "회원가입 이메일 인증 요청"
             :description "회원가입을 위한 이메일 인증 링크를 발송합니다."
             :swagger {:tags ["auth"]}}}]

    ["/verify/confirm"
     {:post {:handler (fn [req]
                        (user/verify-email
                         (assoc req :user-use-case user-use-case)))
             :parameters {:body [:map
                                 [:token :string]]}
             :responses {200 {:body [:map [:message :string]]}
                         400 {:body [:map [:error :string]]}
                         404 {:body [:map [:error :string]]}}
             :summary "회원가입 이메일 인증 완료"
             :description "이메일 인증 토큰을 확인하고 회원가입을 완료합니다."
             :swagger {:tags ["auth"]}}}]

    ["/password"
     ["/reset"
      {:post {:handler (fn [req]
                         (password/request-reset
                          (assoc req :user-use-case user-use-case)))
              :parameters {:body [:map
                                  [:email :string]]}
              :responses {200 {:body [:map [:token :string]]}
                          400 {:body [:map [:error :string]]}
                          404 {:body [:map [:error :string]]}
                          429 {:body [:map [:error :string]]}}
              :summary "비밀번호 초기화 요청"
              :description "이메일을 통해 비밀번호 초기화를 요청합니다."
              :swagger {:tags ["auth"]}}}]

     ["/reset/confirm"
      {:post {:handler (fn [req]
                         (password/reset-password
                          (assoc req :user-use-case user-use-case)))
              :parameters {:body [:map
                                  [:token :string]
                                  [:new-password :string]]}
              :responses {200 {:body [:map [:user-uuid :string]]}
                          400 {:body [:map [:error :string]]}
                          401 {:body [:map [:error :string]]}
                          404 {:body [:map [:error :string]]}}
              :summary "비밀번호 초기화 완료"
              :description "토큰을 사용하여 새로운 비밀번호로 변경합니다."
              :swagger {:tags ["auth"]}}}]]
              ]])
