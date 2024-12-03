(ns kit.spooky-town.web.controllers.password-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.web.controllers.password :as password]
            [kit.spooky-town.domain.user.test.repository :as user-repository-fixture :refer [->TestUserRepository]]
            [kit.spooky-town.domain.user.test.password-gateway :as password-gateway-fixture :refer [->TestPasswordGateway]]
            [kit.spooky-town.domain.user.test.token-gateway :as token-gateway-fixture :refer [->TestTokenGateway]]
            [kit.spooky-town.domain.user.use-case :refer [->UserUseCaseImpl]]
            [kit.spooky-town.domain.event.test.subscriber :refer [->TestEventSubscriber]]))

(deftest request-password-reset-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber)
        test-email "test@example.com"
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"]

    (testing "유효한 이메일로 비밀번호 초기화 요청"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:id 1
                       :uuid test-uuid
                       :email test-email})
                    token-gateway-fixture/find-valid-token (fn [_ _] nil)
                    token-gateway-fixture/check-rate-limit (fn [_ _ _] false)
                    token-gateway-fixture/generate (fn [_ _ _] "valid-token")]
        (let [request {:body-params {:email test-email}
                      :user-use-case user-use-case}
              response (password/request-reset request)]
          (is (= 200 (:status response)))
          (is (= "valid-token" (get-in response [:body :token]))))))

    (testing "유효하지 않은 이메일로 요청"
      (let [request {:body-params {:email "invalid"}
                    :user-use-case user-use-case}
            response (password/request-reset request)]
        (is (= 400 (:status response)))
        (is (= "유효하지 않은 이메일입니다" (get-in response [:body :error])))))

    (testing "존재하지 않는 사용자 이메일로 요청"
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)]
        (let [request {:body-params {:email test-email}
                      :user-use-case user-use-case}
              response (password/request-reset request)]
          (is (= 404 (:status response)))
          (is (= "사용자를 찾을 수 없습니다" (get-in response [:body :error]))))))

    (testing "탈퇴한 사용자의 이메일로 요청"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:id 1
                       :uuid test-uuid
                       :email test-email
                       :deleted-at (java.util.Date.)})]
        (let [request {:body-params {:email test-email}
                      :user-use-case user-use-case}
              response (password/request-reset request)]
          (is (= 400 (:status response)))
          (is (= "탈퇴한 사용자입니다" (get-in response [:body :error]))))))

    (testing "이미 유효한 토큰이 있는 경우"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:id 1
                       :uuid test-uuid
                       :email test-email})
                    token-gateway-fixture/find-valid-token (fn [_ _] "existing-token")]
        (let [request {:body-params {:email test-email}
                      :user-use-case user-use-case}
              response (password/request-reset request)]
          (is (= 400 (:status response)))
          (is (= "이미 유효한 토큰이 존재합니다" (get-in response [:body :error]))))))

    (testing "rate limit 초과"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:id 1
                       :uuid test-uuid
                       :email test-email})
                    token-gateway-fixture/find-valid-token (fn [_ _] nil)
                    token-gateway-fixture/check-rate-limit (fn [_ _ _] true)]
        (let [request {:body-params {:email test-email}
                      :user-use-case user-use-case}
              response (password/request-reset request)]
          (is (= 429 (:status response)))
          (is (= "요청 횟수가 초과되었습니다" (get-in response [:body :error])))))))) 