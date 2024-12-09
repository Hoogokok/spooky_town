(ns kit.spooky-town.domain.user.use-case-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [kit.spooky-town.domain.user.use-case :as use-case :refer [->UserUseCaseImpl]]
            [kit.spooky-town.domain.user.test.password-gateway :as password-gateway-fixture ]
            [kit.spooky-town.domain.user.test.token-gateway :as token-gateway-fixture]
            [kit.spooky-town.domain.user.test.repository :as user-repository-fixture]
            [kit.spooky-town.domain.event.test.subscriber :as event-subscriber-fixture]
            [kit.spooky-town.domain.user.test.email-gateway-fixture :as email-gateway-fixture]
            [kit.spooky-town.domain.user-role.test.repository :as user-role-repository-fixture]
            [kit.spooky-town.domain.role.test.repository :as role-repository-fixture]
            [kit.spooky-town.domain.user.test.email-token-gateway-fixture :as email-token-gateway-fixture]
            [kit.spooky-town.domain.user.test.email-verification-gateway-fixture :as email-verification-gateway-fixture]
            [kit.spooky-town.domain.common.id.test.generator :as id-generator-fixture]
            [kit.spooky-town.domain.common.id.test.uuid-generator :as uuid-generator-fixture]
            [kit.spooky-town.domain.event :as event]))

  ;;(->UserUseCaseImpl with-tx password-gateway token-gateway user-repository user-role-repository role-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator))

(deftest request-password-reset-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        user-repository (user-repository-fixture/->TestUserRepository)
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        id-generator (id-generator-fixture/->TestIdGenerator)
        uuid-generator (uuid-generator-fixture/->TestUuidGenerator)
        use-case (->UserUseCaseImpl
                  with-tx
                  password-gateway
                  token-gateway
                  user-repository
                  user-role-repository
                  role-repository
                  event-subscriber
                  email-gateway
                  email-token-gateway
                  email-verification-gateway
                  id-generator
                  uuid-generator)]

    (testing "유효한 비밀번호 재설정 요청"
  (with-redefs [user-repository-fixture/find-by-email (fn [_ _]
                                                        {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                                                         :email "user@example.com"})
   token-gateway-fixture/find-valid-token (fn [_ _] nil)
   token-gateway-fixture/check-rate-limit (fn [_ _ _] false)
   token-gateway-fixture/generate (fn [_ _ _] "reset-token-123")]
  (let [result (use-case/request-password-reset
                use-case
                {:email "user@example.com"})]
    (is (not (f/failed? result)))
    (is (= "reset-token-123" (:token result))))))

    (testing "존재하지 않는 이메일"
  (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)]
    (let [result (use-case/request-password-reset
              use-case
              {:email "nonexistent@example.com"})]
          (is (f/failed? result))
          (is (= :password-reset/user-not-found (f/message result))))))

    (testing "탈퇴한 사용자"
  (with-redefs [user-repository-fixture/find-by-email (fn [_ _]
                                                      {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                                                       :email "user@example.com"
                                                         :deleted-at (java.util.Date.)})]
    (let [result (use-case/request-password-reset
              use-case
              {:email "user@example.com"})]
  (is (f/failed? result))
          (is (= :password-reset/withdrawn-user (f/message result))))))

    (testing "이미 유효한 토큰이 존재하는 경우"
  (with-redefs [user-repository-fixture/find-by-email (fn [_ _]
                                                        {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                                                         :email "user@example.com"})
                token-gateway-fixture/find-valid-token (fn [_ _] "existing-token")]
    (let [result (use-case/request-password-reset
                  use-case
  {:email "user@example.com"})]
(is (f/failed? result))
          (is (= :password-reset/token-already-exists (f/message result))))))))

(deftest register-user-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        user-repository (user-repository-fixture/->TestUserRepository)
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        id-generator (id-generator-fixture/->TestIdGenerator)
        uuid-generator (uuid-generator-fixture/->TestUuidGenerator)
        use-case (->UserUseCaseImpl
                  with-tx
                  password-gateway
                  token-gateway
                  user-repository
                  user-role-repository
                  role-repository
                  event-subscriber
                  email-gateway
                  email-token-gateway
                  email-verification-gateway
                  id-generator
                  uuid-generator)
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"]

    (testing "유효한 사용자 등록"
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)
                    id-generator-fixture/generate-ulid (fn [_] "user-01")
                    password-gateway-fixture/hash-password (fn [_ _] "hashed_password")
                    role-repository-fixture/find-by-name (fn [_ _]
                                                         {:id "role-01"
                                                          :name :user})
                    user-repository-fixture/save! (fn [_ user]
                                                  (assoc user :uuid test-uuid))
                    user-role-repository-fixture/add-user-role! (fn [_ _ _] true)
                    token-gateway-fixture/generate (fn [_ _ _] "generated_token")]
        (let [result (use-case/register-user
                      use-case
                      {:email "user@example.com"
                       :name "Test User"
                       :password "password123!"})]
          (is (not (f/failed? result)))
          (is (= "generated_token" (:token result)))
          (is (= test-uuid (:user-uuid result))))))

    (testing "이미 존재하는 이메일"
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] 
                                                          {:id "existing-user"
                                                           :email "user@example.com"})]
        (let [result (use-case/register-user
                      use-case
                      {:email "user@example.com"
                       :name "Test User"
                       :password "password123!"})]
          (is (f/failed? result))
          (is (= :registration-error/email-already-exists (f/message result))))))))