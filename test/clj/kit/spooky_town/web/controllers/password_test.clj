(ns kit.spooky-town.web.controllers.password-test
  (:require
   [clojure.test :refer :all]
   [failjure.core :as f]
   [kit.spooky-town.domain.event.test.subscriber :refer [->TestEventSubscriber]]
   [kit.spooky-town.domain.user.test.password-gateway :as password-gateway-fixture :refer [->TestPasswordGateway]]
   [kit.spooky-town.domain.user.test.repository :as user-repository-fixture :refer [->TestUserRepository]]
   [kit.spooky-town.domain.user.test.token-gateway :as token-gateway-fixture :refer [->TestTokenGateway]]
   [kit.spooky-town.domain.user-role.test.repository :as user-role-repository-fixture :refer [->TestUserRoleRepository]]
   [kit.spooky-town.domain.role.test.repository :as role-repository-fixture :refer [->TestRoleRepository]]
   [kit.spooky-town.domain.user.use-case :refer [->UserUseCaseImpl]]
   [kit.spooky-town.domain.common.id.test.generator :as id-generator-fixture :refer [->TestIdGenerator]]
   [kit.spooky-town.domain.user.test.email-gateway-fixture :as email-gateway-fixture :refer [->TestEmailGateway]]
   [kit.spooky-town.domain.user.test.email-token-gateway-fixture :as email-token-gateway-fixture :refer [->TestEmailTokenGateway]]
   [kit.spooky-town.domain.user.test.email-verification-gateway-fixture :as email-verification-gateway-fixture :refer [->TestEmailVerificationGateway]]
   [kit.spooky-town.web.controllers.password :as password]))

(deftest request-password-reset-test
  (let [with-tx (fn [repos f]
                  (let [user-repo (first repos)
                        user-role-repo (second repos)
                        role-repo (nth repos 2)]
                    (f user-repo user-role-repo role-repo)))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        user-role-repository (->TestUserRoleRepository)
        role-repository (->TestRoleRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        id-generator (->TestIdGenerator)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository user-role-repository role-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)
        test-email "test@example.com"
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"]

    (testing "유효한 이메일로 비밀번호 초기화 요청"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:id "user-ulid1"
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
                      {:id "user-ulid1"
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
                      {:id "user-ulid1"
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
                      {:id "user-ulid1"
                       :uuid test-uuid
                       :email test-email})
                    token-gateway-fixture/find-valid-token (fn [_ _] nil)
                    token-gateway-fixture/check-rate-limit (fn [_ _ _] true)]
        (let [request {:body-params {:email test-email}
                      :user-use-case user-use-case}
              response (password/request-reset request)]
          (is (= 429 (:status response)))
          (is (= "요청 횟수가 초과되었습니다" (get-in response [:body :error]))))))))

(deftest reset-password-test
  (let [with-tx (fn [repos f]
                  (let [user-repo (first repos)
                        user-role-repo (second repos)
                        role-repo (nth repos 2)]
                    (f user-repo user-role-repo role-repo)))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        user-role-repository (->TestUserRoleRepository)
        role-repository (->TestRoleRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        id-generator (->TestIdGenerator)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository user-role-repository role-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"]

    (testing "비밀번호 초기화 - 성공"
      (with-redefs [token-gateway-fixture/verify 
                    (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:id "user-ulid1"
                       :uuid test-uuid
                       :email "test@example.com"})
                    password-gateway-fixture/hash-password 
                    (fn [_ _] "hashed-password")
                    user-repository-fixture/save! 
                    (fn [_ user] user)
                    user-role-repository-fixture/find-roles-by-user 
                    (fn [_ _] [{:role-id "role-01HQ..." :role-name :user}])]
        (let [request {:body-params {:token "valid-token"
                                   :new-password "new-password123!"}
                      :user-use-case user-use-case}
              response (password/reset-password request)]
          (is (= 200 (:status response)))
          (is (= test-uuid (get-in response [:body :user-uuid]))))))

    (testing "비밀번호 초기화 - 유효하지 않은 토큰"
      (with-redefs [token-gateway-fixture/verify 
                    (fn [_ _] (f/fail :token-error/invalid))]
        (let [request {:body-params {:token "invalid-token"
                                   :new-password "new-password123!"}
                      :user-use-case user-use-case}
              response (password/reset-password request)]
          (is (= 400 (:status response)))
          (is (= "유효하지 않은 토큰입니다" (get-in response [:body :error]))))))

    (testing "비밀번호 초기화 - 만료된 토큰"
      (with-redefs [token-gateway-fixture/verify 
                    (fn [_ _] (f/fail :token-error/expired))]
        (let [request {:body-params {:token "expired-token"
                                   :new-password "new-password123!"}
                      :user-use-case user-use-case}
              response (password/reset-password request)]
          (is (= 400 (:status response)))
          (is (= "만료된 토큰입니다" (get-in response [:body :error]))))))

    (testing "비밀번호 초기화 - 탈퇴한 사용자"
      (with-redefs [token-gateway-fixture/verify 
                    (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:id "user-ulid1"
                       :uuid test-uuid
                       :email "test@example.com"
                       :roles #{:user}
                       :deleted-at (java.util.Date.)})
                    user-role-repository-fixture/find-roles-by-user 
                    (fn [_ _] [{:role-id 1 :role-name :user}])]
        (let [request {:body-params {:token "valid-token"
                                   :new-password "new-password123!"}
                      :user-use-case user-use-case}
              response (password/reset-password request)]
          (is (= 400 (:status response)))
          (is (= "탈퇴한 사용자입니다" (get-in response [:body :error]))))))

    (testing "비밀번호 초기화 - 유효하지 않은 비밀번호"
      (with-redefs [token-gateway-fixture/verify 
                    (fn [_ _] test-uuid)
                    password-gateway-fixture/hash-password 
                    (fn [_ _] (f/fail :password-reset/invalid-password))
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:id "user-ulid1"
                       :uuid test-uuid
                       :email "test@example.com"})]
        (let [request {:body-params {:token "valid-token"
                                   :new-password "weak"}
                      :user-use-case user-use-case}
              response (password/reset-password request)]
          (is (= 400 (:status response)))
          (is (= "유효하지 않은 비밀번호입니다" (get-in response [:body :error]))))))))

