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
                    uuid-generator-fixture/generate-uuid (constantly #uuid "550e8400-e29b-41d4-a716-446655440000")
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

(deftest authenticate-user-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-repository (user-repository-fixture/->TestUserRepository)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
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

    (testing "유효한 인증 정보로 로그인"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    password-gateway-fixture/verify-password 
                    (fn [_ _ _] true)
                    token-gateway-fixture/generate 
                    (fn [_ _ _] "valid_token")]
        (let [result (use-case/authenticate-user
                      use-case
                      {:email "test@example.com"
                       :password "Valid1!password"})]
          (is (not (f/failed? result)))
          (is (= test-uuid (get-in result [:user-uuid])))
          (is (= "valid_token" (get-in result [:token]))))))

    (testing "존재하지 않는 이메일로 로그인 시도"
      (with-redefs [user-repository-fixture/find-by-email 
                    (fn [_ _] nil)]
        (let [result (use-case/authenticate-user
                      use-case
                      {:email "nonexistent@example.com"
                       :password "Valid1!password"})]
          (is (f/failed? result))
          (is (= :authentication-error/user-not-found (f/message result))))))

    (testing "잘못된 비밀번호로 로그인 시도"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    password-gateway-fixture/verify-password 
                    (fn [_ _ _] false)]
        (let [result (use-case/authenticate-user
                      use-case
                      {:email "test@example.com"
                       :password "WrongPassword1!"})]
          (is (f/failed? result))
          (is (= :authentication-error/invalid-credentials (f/message result))))))

    (testing "탈퇴한 사용자로 로그인 시도"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"
                       :deleted-at (java.util.Date.)})]
        (let [result (use-case/authenticate-user
                      use-case
                      {:email "test@example.com"
                       :password "Valid1!password"})]
          (is (f/failed? result))
          (is (= :authentication-error/withdrawn-user (f/message result))))))))

(deftest update-user-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-repository (user-repository-fixture/->TestUserRepository)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
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

    (testing "유효한 정보로 사용자 정보 업데이트"
      (with-redefs [token-gateway-fixture/verify
                    (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _]
                      {:uuid test-uuid
                       :email "test@example.com"
                       :name "Test User"})
                    user-repository-fixture/find-by-email
                    (fn [_ _] nil)
                    password-gateway-fixture/hash-password
                    (fn [_ _] "new_hashed_password")
                    user-repository-fixture/save!
                    (fn [_ user] user)]
        (let [result (use-case/update-user
                      use-case
                      {:token "valid_token"
                       :name "New Name"
                       :email "new@example.com"
                       :password "NewPassword1!"})]
          (is (not (f/failed? result)))
          (is (= test-uuid (:user-uuid result)))
          (is (= "new@example.com" (:email result)))
          (is (= "New Name" (:name result))))))

    (testing "유효하지 않은 토큰으로 업데이트 시도"
      (with-redefs [token-gateway-fixture/verify
                    (fn [_ _] nil)]
        (let [result (use-case/update-user
                      use-case
                      {:token "invalid_token"
                       :name "New Name"})]
          (is (f/failed? result))
          (is (= :update-error/invalid-token (f/message result))))))

    (testing "존재하지 않는 사용자 업데이트 시도"
      (with-redefs [token-gateway-fixture/verify
                    (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _] nil)]
        (let [result (use-case/update-user
                      use-case
                      {:token "valid_token"
                       :name "New Name"})]
          (is (f/failed? result))
          (is (= :update-error/user-not-found (f/message result))))))

    (testing "탈퇴한 사용자 업데이트 시도"
      (with-redefs [token-gateway-fixture/verify
                    (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _]
                      {:uuid test-uuid
                       :email "test@example.com"
                       :name "Test User"
                       :deleted-at (java.util.Date.)})]
        (let [result (use-case/update-user
                      use-case
                      {:token "valid_token"
                       :name "New Name"})]
          (is (f/failed? result))
          (is (= :update-error/withdrawn-user (f/message result))))))

    (testing "이미 존재하는 이메일로 업데이트 시도"
      (with-redefs [token-gateway-fixture/verify
                    (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _]
                      {:uuid test-uuid
                       :email "test@example.com"
                       :name "Test User"})
                    user-repository-fixture/find-by-email
                    (fn [_ _]
                      {:uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :email "existing@example.com"})]
        (let [result (use-case/update-user
                      use-case
                      {:token "valid_token"
                       :email "existing@example.com"})]
          (is (f/failed? result))
          (is (= :update-error/email-already-exists (f/message result))))))))

(deftest update-user-role-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-repository (user-repository-fixture/->TestUserRepository)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
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
        admin-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"]

    (testing "관리자가 유효한 사용자 역할 업데이트"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) "admin-01HQ..." "user-01HQ..."))
                    user-repository-fixture/find-by-id
                    (fn [_ id]
                      (case id
                        "admin-01HQ..." {:id "admin-01HQ..."
                                         :uuid admin-uuid
                                         :email "admin@example.com"}
                        "user-01HQ..." {:id "user-01HQ..."
                                        :uuid user-uuid
                                        :email "user@example.com"}))
                    user-role-repository-fixture/find-roles-by-user
                    (fn [_ id]
                      (if (= id "admin-01HQ...")
                        [{:role-id "role-01HQ..." :role-name :admin}]
                        [{:role-id "role-02HQ..." :role-name :user}]))
                    role-repository-fixture/find-by-id
                    (fn [_ _]
                      {:role-id "role-03HQ..."
                       :role-name :content-creator})
                    user-role-repository-fixture/add-user-role!
                    (fn [_ _ _] nil)]
        (let [result (use-case/update-user-role
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :role-id "role-03HQ..."})]
          (is (not (f/failed? result)))
          (is (= user-uuid (:user-uuid result)))
          (is (= "role-03HQ..." (get-in result [:role :role-id])))
          (is (= :content-creator (get-in result [:role :role-name]))))))

    (testing "관리자가 아닌 사용자의 역할 업데이트 시도"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ _] "user-01HQ...")
                    user-role-repository-fixture/find-roles-by-user
                    (fn [_ _] [{:role-id "role-02HQ..." :role-name :user}])]
        (let [result (use-case/update-user-role
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :role-id "role-03HQ..."})]
          (is (f/failed? result))
          (is (= :update-error/insufficient-permissions (f/message result))))))

    (testing "존재하지 않는 관리자"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ _] nil)]
        (let [result (use-case/update-user-role
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :role-id "role-03HQ..."})]
          (is (f/failed? result))
          (is (= :admin/not-found (f/message result))))))

    (testing "존재하지 않는 사용자"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid)
                        "admin-01HQ..."
                        nil))
                    user-role-repository-fixture/find-roles-by-user
                    (fn [_ _] [{:role-id "role-01HQ..." :role-name :admin}])]
        (let [result (use-case/update-user-role
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :role-id "role-03HQ..."})]
          (is (f/failed? result))
          (is (= :user/not-found (f/message result))))))

    (testing "탈퇴한 사용자"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) "admin-01HQ..." "user-01HQ..."))
                    user-repository-fixture/find-by-id
                    (fn [_ id]
                      (case id
                        "admin-01HQ..." {:id "admin-01HQ..."
                                         :uuid admin-uuid
                                         :email "admin@example.com"}
                        "user-01HQ..." {:id "user-01HQ..."
                                        :uuid user-uuid
                                        :email "user@example.com"
                                        :deleted-at (java.util.Date.)}))
                    user-role-repository-fixture/find-roles-by-user
                    (fn [_ id]
                      (if (= id "admin-01HQ...")
                        [{:role-id "role-01HQ..." :role-name :admin}]
                        [{:role-id "role-02HQ..." :role-name :user}]))]
        (let [result (use-case/update-user-role
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :role-id "role-03HQ..."})]
          (is (f/failed? result))
          (is (= :update-error/withdrawn-user (f/message result))))))))

(deftest withdraw-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-repository (user-repository-fixture/->TestUserRepository)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
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

    (testing "유효한 정보로 회원 탈퇴"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ _]
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    password-gateway-fixture/verify-password
                    (fn [_ _ _] true)
                    user-repository-fixture/save!
                    (fn [_ user] user)]
        (let [result (use-case/withdraw
                      use-case
                      {:user-uuid test-uuid
                       :password "Valid1!password"
                       :reason "개인 사유"})]
          (is (not (f/failed? result)))
          (is (:success result)))))

    (testing "유효하지 않은 비밀번호 형식"
      (let [result (use-case/withdraw
                    use-case
                    {:user-uuid test-uuid
                     :password "weak"
                     :reason "개인 사유"})]
        (is (f/failed? result))
        (is (= :withdrawal-error/invalid-password (f/message result)))))

    (testing "존재하지 않는 사용자"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ _] nil)]
        (let [result (use-case/withdraw
                      use-case
                      {:user-uuid test-uuid
                       :password "Valid1!password"
                       :reason "개인 사유"})]
          (is (f/failed? result))
          (is (= :withdrawal-error/user-not-found (f/message result))))))

    (testing "이미 탈퇴한 사용자"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ _]
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"
                       :deleted-at (java.util.Date.)})]
        (let [result (use-case/withdraw
                      use-case
                      {:user-uuid test-uuid
                       :password "Valid1!password"
                       :reason "개인 사유"})]
          (is (f/failed? result))
          (is (= :withdrawal-error/already-withdrawn (f/message result))))))

    (testing "잘못된 비밀번호"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ _]
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    password-gateway-fixture/verify-password
                    (fn [_ _ _] false)]
        (let [result (use-case/withdraw
                      use-case
                      {:user-uuid test-uuid
                       :password "WrongPassword1!"
                       :reason "개인 사유"})]
          (is (f/failed? result))
          (is (= :withdrawal-error/invalid-credentials (f/message result))))))))

(deftest delete-user-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-repository (user-repository-fixture/->TestUserRepository)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
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
        admin-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"]

    (testing "관리자가 유효한 사용자 삭제"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) "admin-01HQ..." "user-01HQ..."))
                    user-repository-fixture/find-by-uuid
                    (fn [_ uuid]
                      (if (= uuid user-uuid)
                        {:id "user-01HQ..."
                         :uuid user-uuid
                         :email "user@example.com"}))
                    user-role-repository-fixture/find-roles-by-user
                    (fn [_ id]
                      (if (= id "admin-01HQ...")
                        [{:role-id "role-01HQ..." :role-name :admin}]))
                    user-repository-fixture/save!
                    (fn [_ user] user)]
        (let [result (use-case/delete-user
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :reason "부적절한 활동"})]
          (is (not (f/failed? result)))
          (is (:success result)))))

    (testing "존재하지 않는 관리자"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ _] nil)]
        (let [result (use-case/delete-user
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :reason "부적절한 활동"})]
          (is (f/failed? result))
          (is (= :delete-error/admin-not-found (f/message result))))))

    (testing "관리자 권한이 없는 사용자"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ _] "user-01HQ...")
                    user-role-repository-fixture/find-roles-by-user
                    (fn [_ _] [{:role-id "role-02HQ..." :role-name :user}])]
        (let [result (use-case/delete-user
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :reason "부적절한 활동"})]
          (is (f/failed? result))
          (is (= :delete-error/insufficient-permissions (f/message result))))))

    (testing "존재하지 않는 대상 사용자"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) "admin-01HQ..." nil))
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] nil)
                    user-role-repository-fixture/find-roles-by-user
                    (fn [_ _] [{:role-id "role-01HQ..." :role-name :admin}])]
        (let [result (use-case/delete-user
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :reason "부적절한 활동"})]
          (is (f/failed? result))
          (is (= :delete-error/user-not-found (f/message result))))))

    (testing "이미 탈퇴한 사용자 삭제 시도"
      (with-redefs [user-repository-fixture/find-id-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) "admin-01HQ..." "user-01HQ..."))
                    user-repository-fixture/find-by-uuid
                    (fn [_ uuid]
                      (if (= uuid user-uuid)
                        {:id "user-01HQ..."
                         :uuid user-uuid
                         :email "user@example.com"
                         :deleted-at (java.util.Date.)}))
                    user-role-repository-fixture/find-roles-by-user
                    (fn [_ _] [{:role-id "role-01HQ..." :role-name :admin}])]
        (let [result (use-case/delete-user
                      use-case
                      {:admin-uuid admin-uuid
                       :user-uuid user-uuid
                       :reason "부적절한 활동"})]
          (is (f/failed? result))
          (is (= :delete-error/already-withdrawn (f/message result))))))))

(deftest request-email-verification-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-repository (user-repository-fixture/->TestUserRepository)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
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
        test-email "test@example.com"]

    (testing "회원가입 이메일 검증 요청"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ email]
                      {:email email
                       :id "user-01HQ..."})
                    email-token-gateway-fixture/generate-token
                    (fn [_ email purpose]
                      (when (and (= email test-email)
                                 (= purpose :registration))
                        "valid_token"))
                    email-gateway-fixture/send-verification-email
                    (fn [_ email token]
                      (when (and (= email test-email)
                                 (= token "valid_token"))
                        true))]
        (let [result (use-case/request-email-verification
                      use-case
                      test-email
                      :registration)]
          (is (not (f/failed? result)))
          (is (= "valid_token" (:token result))))))

    (testing "비밀번호 재설정 이메일 검증 요청"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ email]
                      {:email email
                       :id "user-01HQ..."})
                    email-token-gateway-fixture/generate-token
                    (fn [_ email purpose]
                      (when (and (= email test-email)
                                 (= purpose :password-reset))
                        "valid_token"))
                    email-gateway-fixture/send-password-reset-email
                    (fn [_ email token]
                      (when (and (= email test-email)
                                 (= token "valid_token"))
                        true))]
        (let [result (use-case/request-email-verification
                      use-case
                      test-email
                      :password-reset)]
          (is (not (f/failed? result)))
          (is (= "valid_token" (:token result))))))

    (testing "이메일 변경 검증 요청"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ email]
                      {:email email
                       :id "user-01HQ..."})
                    email-token-gateway-fixture/generate-token
                    (fn [_ email purpose]
                      (when (and (= email test-email)
                                 (= purpose :email-change))
                        "valid_token"))
                    email-gateway-fixture/send-email-change-verification
                    (fn [_ email token]
                      (when (and (= email test-email)
                                 (= token "valid_token"))
                        true))]
        (let [result (use-case/request-email-verification
                      use-case
                      test-email
                      :email-change)]
          (is (not (f/failed? result)))
          (is (= "valid_token" (:token result))))))

    (testing "존재하지 않는 사용자"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] nil)]
        (let [result (use-case/request-email-verification
                      use-case
                      test-email
                      :registration)]
          (is (f/failed? result))
          (is (= :email-verification/user-not-found (f/message result))))))

    (testing "유효하지 않은 목적"
      (let [result (use-case/request-email-verification
                    use-case
                    test-email
                    :invalid)]
        (is (f/failed? result))
        (is (= :email-verification/invalid-purpose (f/message result)))))))

(deftest verify-email-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-repository (user-repository-fixture/->TestUserRepository)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
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
        test-email "test@example.com"]

    (testing "유효한 토큰으로 이메일 검증"
      (with-redefs [email-token-gateway-fixture/verify-token
                    (fn [_ token]
                      (when (= token "valid_token")
                        {:email test-email
                         :purpose :registration}))
                    email-verification-gateway-fixture/save-verification-status!
                    (fn [_ email purpose status]
                      (when (and (= email test-email)
                                 (= purpose :registration)
                                 (= status :verified))
                        true))
                    email-verification-gateway-fixture/get-verification-status
                    (fn [_ email purpose]
                      (when (and (= email test-email)
                                 (= purpose :registration))
                        :verified))]
        (let [result (use-case/verify-email
                      use-case
                      "valid_token")]
          (is (not (f/failed? result)))
          (is (= test-email (:email result)))
          (is (= :registration (:purpose result))))))

    (testing "빈 토큰으로 이메일 검증 시도"
      (let [result (use-case/verify-email
                    use-case
                    "")]
        (is (f/failed? result))
        (is (= :email-verification/invalid-token (f/message result)))))

    (testing "유효하지 않은 토큰으로 이메일 검증 시도"
      (with-redefs [email-token-gateway-fixture/verify-token
                    (fn [_ _]
                      (f/fail :email-verification/invalid-token))]
        (let [result (use-case/verify-email
                      use-case
                      "invalid_token")]
          (is (f/failed? result))
          (is (= :email-verification/invalid-token (f/message result))))))))

(deftest check-email-verification-status-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    3 (f (first repos) (second repos) (nth repos 2))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        password-gateway (password-gateway-fixture/->TestPasswordGateway)
        token-gateway (token-gateway-fixture/->TestTokenGateway)
        user-repository (user-repository-fixture/->TestUserRepository)
        user-role-repository (user-role-repository-fixture/->TestUserRoleRepository)
        role-repository (role-repository-fixture/->TestRoleRepository)
        event-subscriber (event-subscriber-fixture/->TestEventSubscriber)
        email-gateway (email-gateway-fixture/->TestEmailGateway)
        email-token-gateway (email-token-gateway-fixture/->TestEmailTokenGateway)
        email-verification-gateway (email-verification-gateway-fixture/->TestEmailVerificationGateway)
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
        test-email "test@example.com"]

    (testing "검증된 이메일 상태 확인"
      (with-redefs [email-verification-gateway-fixture/get-verification-status
                    (fn [_ email purpose]
                      (when (and (= email test-email)
                                 (= purpose :registration))
                        :verified))]
        (let [result (use-case/check-email-verification-status
                      use-case
                      test-email)]
          (is (not (f/failed? result)))
          (is (= test-email (:email result)))
          (is (= :verified (:status result))))))

    (testing "검증되지 않은 이메일 상태 확인"
      (with-redefs [email-verification-gateway-fixture/get-verification-status
                    (fn [_ _ _] nil)]
        (let [result (use-case/check-email-verification-status
                      use-case
                      test-email)]
          (is (f/failed? result))
          (is (= :email-verification/not-verified (f/message result))))))

    (testing "빈 이메일로 상태 확인 시도"
      (let [result (use-case/check-email-verification-status
                    use-case
                    "")]
        (is (f/failed? result))
        (is (= :email-verification/invalid-email (f/message result)))))))