(ns kit.spooky-town.domain.user.use-case-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [kit.spooky-town.domain.user.use-case :as use-case :refer [->UserUseCaseImpl]]
            [kit.spooky-town.domain.user.test.password-gateway :as password-gateway-fixture :refer [->TestPasswordGateway]]
            [kit.spooky-town.domain.user.test.token-gateway :as token-gateway-fixture :refer [->TestTokenGateway]]
            [kit.spooky-town.domain.user.test.repository :as user-repository-fixture :refer [->TestUserRepository]]
            [kit.spooky-town.domain.event.test.subscriber :as event-subscriber-fixture :refer [->TestEventSubscriber]]
            [kit.spooky-town.domain.user.test.email-gateway-fixture :as email-gateway-fixture :refer [->TestEmailGateway]]
            [kit.spooky-town.domain.user.test.email-token-gateway-fixture :as email-token-gateway-fixture :refer 
             [->TestEmailTokenGateway]]
            [kit.spooky-town.domain.user.test.email-verification-gateway-fixture :as email-verification-gateway-fixture :refer 
             [->TestEmailVerificationGateway]]
            [kit.spooky-town.domain.event :as event]))

(deftest register-user-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway)]

    (testing "유효한 데이터로 사용자 등록"
      (with-redefs [password-gateway-fixture/hash-password (fn [_ password] "hashed_password")
                    user-repository-fixture/find-by-email (fn [_ _] nil)
                    user-repository-fixture/save! (fn [_ _] true)
                    token-gateway-fixture/generate (fn [_ _ _] "generated_token")]
        (let [command {:email "test@example.com"
                       :name "Test User"
                       :password "Valid1!password"}
              result (use-case/register-user user-use-case command)]
          (is (f/ok? result))
(is (uuid? (:user-uuid result)))
          (is (= "generated_token" (:token result))))))

    (testing "이미 존재하는 이메일로 등록 시도"
      (with-redefs [password-gateway-fixture/hash-password (fn [_ password] "hashed_password")
                    user-repository-fixture/find-by-email (fn [_ _] {:email "test@example.com"})]
        (let [command {:email "test@example.com"
                       :name "Test User"
                       :password "Valid1!password"}
              result (use-case/register-user user-use-case command)]
          (is (f/failed? result))
          (is (= :registration-error/email-already-exists (f/message result))))))))

(deftest authenticate-user-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway)]

    (testing "유효한 자격 증명으로 인증"
      (with-redefs [user-repository-fixture/find-by-email 
                    (fn [_ _] 
                      {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    password-gateway-fixture/verify-password (fn [_ _ _] true)
                    token-gateway-fixture/generate (fn [_ _ _] "generated_token")]
        (let [command {:email "test@example.com"
                      :password "Valid1!password"}
              result (use-case/authenticate-user user-use-case command)]
          (is (f/ok? result))
          (is (= #uuid "550e8400-e29b-41d4-a716-446655440000" (:user-uuid result)))
          (is (= "generated_token" (:token result))))))

    (testing "존재하지 않는 이메일로 인증 시도"
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)]
        (let [command {:email "nonexistent@example.com"
                      :password "Valid1!password"}
              result (use-case/authenticate-user user-use-case command)]
          (is (f/failed? result))
          (is (= :authentication-error/user-not-found (f/message result))))))

    (testing "잘못된 비밀번호로 인증 시도"
      (with-redefs [user-repository-fixture/find-by-email 
                    (fn [_ _] 
                      {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    password-gateway-fixture/verify-password (fn [_ _ _] false)]
        (let [command {:email "test@example.com"
                      :password "WrongPassword1!"}
              result (use-case/authenticate-user user-use-case command)]
          (is (f/failed? result))
          (is (= :authentication-error/invalid-credentials (f/message result)))))) 
    (testing  "탈퇴한 사용자로 인증 시도" 
      (with-redefs [user-repository-fixture/find-by-email 
                    (fn [_ _] 
                      {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :email "test@example.com"
                       :deleted-at (java.util.Date.)})]
        (let [command {:email "test@example.com"
                      :password "Valid1!password"}
              result (use-case/authenticate-user user-use-case command)]
          (is (f/failed? result))
          (is (= :authentication-error/withdrawn-user (f/message result))))))))

(deftest update-user-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway) 
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"]

    (testing "유효한 데이터로 사용자 정보 업데이트"
      (with-redefs [token-gateway-fixture/verify (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "old@example.com"
                       :name "Old Name"})
                    user-repository-fixture/find-by-email (fn [_ _] nil)
                    user-repository-fixture/save! (fn [_ user] user)]
        (let [command {:token "valid-token"
                       :name "New Name"
                       :email "new@example.com"}
              result (use-case/update-user user-use-case command)]
          (is (= test-uuid (:user-uuid result)))
          (is (= "new@example.com" (:email result)))
          (is (= "New Name" (:name result))))))

    (testing "이미 존재하는 이메일로 업데이트 시도"
      (with-redefs [token-gateway-fixture/verify (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "old@example.com"
                       :name "Old Name"})
                    user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :email "existing@example.com"})]
        (let [command {:token "valid-token"
                       :email "existing@example.com"}
              result (use-case/update-user user-use-case command)]
          (is (f/failed? result))
          (is (= :update-error/email-already-exists (f/message result))))))

    (testing "존재하지 않는 사용자"
      (with-redefs [token-gateway-fixture/verify (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id (fn [_ _] nil)]
        (let [command {:token "valid-token"
                       :name "New Name"}
              result (use-case/update-user user-use-case command)]
          (is (f/failed? result))
          (is (= :update-error/user-not-found (f/message result))))))

    (testing "탈퇴한 사용자"
      (with-redefs [token-gateway-fixture/verify (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "old@example.com"
                       :name "Old Name"
                       :deleted-at (java.util.Date.)})]
        (let [command {:token "valid-token"
                       :name "New Name"}
              result (use-case/update-user user-use-case command)]
          (is (f/failed? result))
          (is (= :update-error/withdrawn-user (f/message result))))))))

(deftest update-user-role-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway)
        admin-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"]

    (testing "유효한 UUID로 역할 업데이트"
      (with-redefs [user-repository-fixture/find-id-by-uuid 
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) 1 2))
                    user-repository-fixture/find-by-id
                    (fn [_ id] 
                      (case id
                        1 {:id 1
                           :uuid admin-uuid
                           :email "admin@example.com"
                           :roles #{:admin}}
                        2 {:id 2
                           :uuid user-uuid
                           :email "user@example.com"
                           :roles #{:user}}))
                    user-repository-fixture/save! (fn [_ user] user)]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :role :admin}
              result (use-case/update-user-role user-use-case command)]
          (is (= user-uuid (:user-uuid result)))
          (is (= #{:admin} (:roles result))))))

    (testing "존재하지 않는 UUID로 역할 업데이트 시도"
      (with-redefs [user-repository-fixture/find-id-by-uuid (fn [_ _] nil)]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :role :admin}
              result (use-case/update-user-role user-use-case command)]
          (is (f/failed? result))
          (is (= :admin/not-found (f/message result))))))

    (testing "관리자가 아닌 사용자의 역할 업데이트 시도"
      (with-redefs [user-repository-fixture/find-id-by-uuid 
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) 1 2))
                    user-repository-fixture/find-by-id
                    (fn [_ id] 
                      (case id
                        1 {:id 1
                           :uuid admin-uuid
                           :email "admin@example.com"
                           :roles #{:user}}
                        2 {:id 2
                           :uuid user-uuid
                           :email "user@example.com"
                           :roles #{:user}}))]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :role :admin}
              result (use-case/update-user-role user-use-case command)]
          (is (f/failed? result))
          (is (= :update-error/insufficient-permissions (f/message result))))))

    (testing "탈퇴한 사용자의 역할 업데이트 시도"
      (with-redefs [user-repository-fixture/find-id-by-uuid 
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) 1 2))
                    user-repository-fixture/find-by-id
                    (fn [_ id] 
                      (case id
                        1 {:id 1
                           :uuid admin-uuid
                           :email "admin@example.com"
                           :roles #{:admin}}
                        2 {:id 2
                           :uuid user-uuid
                           :email "user@example.com"
                           :roles #{:user}
                           :deleted-at (java.util.Date.)}))]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :role :admin}
              result (use-case/update-user-role user-use-case command)]
          (is (f/failed? result))
          (is (= :update-error/withdrawn-user (f/message result))))))))


(deftest withdraw-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway)
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"]

    (testing "유효한 비밀번호로 회원 탈퇴"
      (with-redefs [password-gateway-fixture/verify-password (fn [_ _ _] true)
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    user-repository-fixture/save! (fn [_ user] user)]
        (let [command {:user-uuid test-uuid
                      :password "Valid1!password"
                      :reason "테스트 탈퇴"}
              result (use-case/withdraw user-use-case command)]
          (is (f/ok? result))
          (is (:success result)))))

    (testing "존재하지 않는 사용자"
      (with-redefs [user-repository-fixture/find-by-uuid (fn [_ _] nil)]
        (let [command {:user-uuid test-uuid
                      :password "Valid1!password"}
              result (use-case/withdraw user-use-case command)]
          (is (f/failed? result))
          (is (= :withdrawal-error/user-not-found (f/message result))))))

    (testing "이미 탈퇴한 용자"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :deleted-at (java.util.Date.)})]
        (let [command {:user-uuid test-uuid
                      :password "Valid1!password"}
              result (use-case/withdraw user-use-case command)]
          (is (f/failed? result))
          (is (= :withdrawal-error/already-withdrawn (f/message result))))))

    (testing "잘못된 비밀번호로 탈퇴 시도"
      (with-redefs [password-gateway-fixture/verify-password (fn [_ _ _] false)
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :hashed-password "hashed_password"})]
        (let [command {:user-uuid test-uuid
                      :password "WrongPassword1!"}
              result (use-case/withdraw user-use-case command)]
          (is (f/failed? result))
          (is (= :withdrawal-error/invalid-credentials (f/message result))))))))

(deftest delete-user-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway)
        admin-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"]

    (testing "관리자가 유효한 사용자 탈퇴 처리"
      (with-redefs [user-repository-fixture/find-by-id
                    (fn [_ uuid]
                      (if (= uuid admin-uuid)
                        {:uuid admin-uuid
                         :email "admin@example.com"
                         :roles #{:admin}}
                        {:uuid user-uuid
                         :email "user@example.com"
                         :roles #{:user}}))
                    user-repository-fixture/save! (fn [_ user] user)]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :reason "관리자에 의한 탈퇴"}
              result (use-case/delete-user user-use-case command)]
          (is (f/ok? result))
          (is (:success result)))))

    (testing "관리자 권한이 없는 경우"
      (with-redefs [user-repository-fixture/find-by-id
                    (fn [_ _]
                      {:uuid admin-uuid
                       :email "user@example.com"
                       :roles #{:user}})]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :reason "권한 없는 탈퇴 시도"}
              result (use-case/delete-user user-use-case command)]
          (is (f/failed? result))
          (is (= :delete-error/insufficient-permissions (f/message result))))))

    (testing "존재하지 않는 관리자"
      (with-redefs [user-repository-fixture/find-by-id (fn [_ _] nil)]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :reason "존재하지 않는 관리자"}
              result (use-case/delete-user user-use-case command)]
          (is (f/failed? result))
          (is (= :delete-error/admin-not-found (f/message result))))))

    (testing "존재하지 않는 사용자"
      (with-redefs [user-repository-fixture/find-by-id
                    (fn [_ uuid]
                      (if (= uuid admin-uuid)
                        {:uuid admin-uuid
                         :email "admin@example.com"
                         :roles #{:admin}}
                        nil))]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :reason "존재하지 않는 사용자"}
              result (use-case/delete-user user-use-case command)]
          (is (f/failed? result))
          (is (= :delete-error/user-not-found (f/message result))))))

    (testing "이미 탈퇴한 사용자"
      (with-redefs [user-repository-fixture/find-by-id
                    (fn [_ uuid]
                      (if (= uuid admin-uuid)
                        {:uuid admin-uuid
                         :email "admin@example.com"
                         :roles #{:admin}}
                        {:uuid user-uuid
                         :email "user@example.com"
                         :roles #{:user}
                         :deleted-at (java.util.Date.)}))]
        (let [command {:admin-uuid admin-uuid
                      :user-uuid user-uuid
                      :reason "이미 탈퇴한 사용자"}
              result (use-case/delete-user user-use-case command)]
          (is (f/failed? result))
          (is (= :delete-error/already-withdrawn (f/message result))))))))

(deftest password-reset-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway)
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        test-email "test@example.com"]

    (testing "비밀번호 초기화 요청 - 성공"
      (with-redefs [user-repository-fixture/find-by-email 
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email test-email})
                    token-gateway-fixture/generate 
                    (fn [_ _ _] "test-token")]
        (let [result (use-case/request-password-reset 
                      user-use-case 
                      {:email test-email})]
          (is (f/ok? result))
          (is (= "test-token" (:token result))))))

    (testing "비밀번호 초기화 - 성공"
      (with-redefs [token-gateway-fixture/verify 
                    (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email test-email})
                    password-gateway-fixture/hash-password 
                    (fn [_ _] "hashed_password")
                    user-repository-fixture/save! 
                    (fn [_ user] user)]
        (let [result (use-case/reset-password 
                      user-use-case 
                      {:token "valid-token"
                       :new-password "NewPassword123!"})]
          (is (f/ok? result))
          (is (= test-uuid (:user-uuid result))))))

    (testing "비밀번호 초기화 - 유효하지 않은 토큰"
      (with-redefs [token-gateway-fixture/verify 
                    (fn [_ _] (f/fail :token-error/invalid))]
        (let [result (use-case/reset-password
                      user-use-case
                      {:token "invalid-token"
                       :new-password "NewPassword123!"})]
          (is (f/failed? result))
          (is (= :token-error/invalid (f/message result))))))

    (testing "비밀번호 초기화 - 탈퇴한 사용자"
      (with-redefs [token-gateway-fixture/verify 
                    (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-uuid
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email test-email
                       :deleted-at (java.util.Date.)})]
        (let [result (use-case/reset-password
                      user-use-case
                      {:token "valid-token"
                       :new-password "NewPassword123!"})]
          (is (f/failed? result))
          (is (= :password-reset/withdrawn-user (f/message result))))))))

(deftest email-verification-use-case-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx 
                                        password-gateway 
                                        token-gateway 
                                        user-repository 
                                        event-subscriber 
                                        email-gateway 
                                        email-token-gateway
                                        email-verification-gateway)]

    (testing "이메일 인증 상태 확인 - 인증된 이메일"
      (with-redefs [email-verification-gateway-fixture/get-verification-status 
                    (fn [_ _ _] 
                      {:status :verified
                       :verified-at (java.util.Date.)})]
        (let [result (use-case/check-email-verification-status 
                      user-use-case 
                      "test@example.com")]
          (is (f/ok? result))
          (is (= :verified (get-in result [:status :status]))))))

    (testing "이메일 인증 상태 확인 - 인증되지 않은 이메일"
      (with-redefs [email-verification-gateway-fixture/get-verification-status 
                    (fn [_ _ _] nil)]
        (let [result (use-case/check-email-verification-status 
                      user-use-case 
                      "test@example.com")]
          (is (f/failed? result))
          (is (= :email-verification/not-verified (f/message result))))))

    (testing "이메일 인증 상태 확인 - 빈 이메일"
      (let [result (use-case/check-email-verification-status user-use-case "")]
        (is (f/failed? result))
        (is (= :email-verification/invalid-email (f/message result)))))

    (testing "이메일 인증 완료 - 성공"
      (with-redefs [email-token-gateway-fixture/verify-token 
                    (fn [_ _] 
                      {:email "test@example.com" 
                       :purpose :registration})
                    email-verification-gateway-fixture/save-verification-status!
                    (fn [_ _ _ _] true)]
        (let [result (use-case/verify-email user-use-case "valid-token")]
          (is (f/ok? result))
          (is (= "test@example.com" (:email result)))
          (is (= :registration (:purpose result))))))

    (testing "이메일 인증 완료 - 실패 (저장 실패)"
      (with-redefs [email-token-gateway-fixture/verify-token 
                    (fn [_ _] 
                      {:email "test@example.com" 
                       :purpose :registration})
                    email-verification-gateway-fixture/save-verification-status!
                    (fn [_ _ _ _] 
                      (f/fail :email-verification/save-failed))]
        (let [result (use-case/verify-email user-use-case "valid-token")]
          (is (f/failed? result))
          (is (= :email-verification/invalid-token (f/message result))))))))