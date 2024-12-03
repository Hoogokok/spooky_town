(ns kit.spooky-town.web.controllers.user-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.web.controllers.user :as user]
            [kit.spooky-town.domain.user.test.repository :as user-repository-fixture :refer [->TestUserRepository]]
            [kit.spooky-town.domain.user.test.password-gateway :as password-gateway-fixture :refer [->TestPasswordGateway]]
            [kit.spooky-town.domain.user.test.token-gateway :as token-gateway-fixture :refer [->TestTokenGateway]]
            [kit.spooky-town.domain.event.test.subscriber :refer [->TestEventSubscriber]] 
            [kit.spooky-town.domain.user.use-case :refer [->UserUseCaseImpl]]
            
            [ring.mock.request :as mock]))

(deftest withdraw-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber)
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        auth-user {:uuid test-uuid
                  :email "test@example.com"}]

    (testing "유효한 비밀번호로 회원 탈퇴"
      (with-redefs [password-gateway-fixture/verify-password (fn [_ _ _] true)
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    user-repository-fixture/save! (fn [_ user] user)]
        (let [request {:body-params {:password "Valid1!password"
                                   :reason "테스트 탈퇴"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/withdraw request)]
          (is (= 204 (:status response))))))

    (testing "잘못된 비밀번호로 탈퇴 시도"
      (with-redefs [password-gateway-fixture/verify-password (fn [_ _ _] false)
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"})]
        (let [request {:body-params {:password "WrongPassword1!"
                                   :reason "테스트 탈퇴"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/withdraw request)]
          (is (= 401 (:status response)))
          (is (= "비밀번호가 일치하지 않습니다" (get-in response [:body :error]))))))

    (testing "이미 탈퇴한 사용자"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :deleted-at (java.util.Date.)})]
        (let [request {:body-params {:password "Valid1!password"
                                   :reason "테스트 탈퇴"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/withdraw request)]
          (is (= 400 (:status response)))
          (is (= "이미 탈퇴한 사용자입니다" (get-in response [:body :error])))))))) 