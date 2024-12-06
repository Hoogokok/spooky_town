(ns kit.spooky-town.web.controllers.user-test
  (:require
   [clojure.test :refer :all]
   [failjure.core :as f]
   [kit.spooky-town.domain.event.test.subscriber :refer [->TestEventSubscriber]]
   [kit.spooky-town.domain.user.test.email-gateway-fixture :as email-gateway-fixture :refer [->TestEmailGateway]]
   [kit.spooky-town.domain.user.test.email-token-gateway-fixture :as email-token-gateway-fixture :refer [->TestEmailTokenGateway]]
   [kit.spooky-town.domain.user.test.email-verification-gateway-fixture :as email-verification-gateway-fixture :refer [->TestEmailVerificationGateway]]
   [kit.spooky-town.domain.user.test.password-gateway :as password-gateway-fixture :refer [->TestPasswordGateway]]
   [kit.spooky-town.domain.user.test.repository :as user-repository-fixture :refer [->TestUserRepository]]
   [kit.spooky-town.domain.user.test.token-gateway :as token-gateway-fixture :refer [->TestTokenGateway]]
   [kit.spooky-town.domain.common.id.test.generator :as id-generator-fixture :refer [->TestIdGenerator]]
   [kit.spooky-town.domain.user.use-case :refer [->UserUseCaseImpl]]
   [kit.spooky-town.web.controllers.user :as user]))

(deftest withdraw-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        id-generator (->TestIdGenerator)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)
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

(deftest delete-user-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        id-generator (->TestIdGenerator)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)
        admin-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
        auth-user {:uuid admin-uuid
                  :email "admin@example.com"}]

    (testing "관리자가 유효한 사용자 탈퇴 처리"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid)
                        {:uuid admin-uuid
                         :email "admin@example.com"
                         :roles #{:admin}}
                        {:uuid user-uuid
                         :email "user@example.com"
                         :roles #{:user}}))
                    user-repository-fixture/save! (fn [_ user] user)]
        (let [request {:path-params {:id (str user-uuid)}
                      :body-params {:reason "관리자에 의한 탈퇴"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/delete-user request)]
          (is (= 204 (:status response))))))

    (testing "관리자 권한이 없는 경우"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ _]
                      {:uuid admin-uuid
                       :email "user@example.com"
                       :roles #{:user}})]
        (let [request {:path-params {:id (str user-uuid)}
                      :body-params {:reason "권한 없는 탈퇴 시도"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/delete-user request)]
          (is (= 403 (:status response)))
          (is (= "관리자 권한이 없습니다" (get-in response [:body :error]))))))

    (testing "존재하지 않는 사용자"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid)
                        {:uuid admin-uuid
                         :email "admin@example.com"
                         :roles #{:admin}}
                        nil))]
        (let [request {:path-params {:id (str user-uuid)}
                      :body-params {:reason "존재하지 않는 사용자"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/delete-user request)]
          (is (= 404 (:status response)))
          (is (= "사용자를 찾을 수 없습니다" (get-in response [:body :error]))))))

    (testing "이미 탈퇴한 사용자"
      (with-redefs [user-repository-fixture/find-by-uuid
                    (fn [_ uuid]
                      (if (= uuid admin-uuid)
                        {:uuid admin-uuid
                         :email "admin@example.com"
                         :roles #{:admin}}
                        {:uuid user-uuid
                         :email "user@example.com"
                         :roles #{:user}
                         :deleted-at (java.util.Date.)}))]
        (let [request {:path-params {:id (str user-uuid)}
                      :body-params {:reason "이미 탈퇴한 사용자"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/delete-user request)]
          (is (= 400 (:status response)))
          (is (= "이미 탈퇴한 사용자입니다" (get-in response [:body :error]))))))))

(deftest register-user-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        id-generator (->TestIdGenerator)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)]

    (testing "유효한 정보로 회원 등록"
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)
                    id-generator-fixture/generate-ulid (fn [_] "generated_ulid")
                    token-gateway-fixture/generate (fn [_ _ _] "generated_token")
                    password-gateway-fixture/hash-password (fn [_ _] "hashed_password")
                    user-repository-fixture/save! (fn [_ user] user)]
        (let [request {:body-params {:email "newuser@example.com"
                                   :name "New User"
                                   :password "Valid1!password"}
                      :user-use-case user-use-case}
              response (user/register request)]
          (is (= 201 (:status response)))
          (is (some? (get-in response [:body :token]))))))

    (testing "이미 존재하는 이메일로 회원 등록 시도"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _]
                      {:email "existinguser@example.com"
                       :name "Existing User"})
                    id-generator-fixture/generate-ulid (fn [_] "generated_ulid")
                    password-gateway-fixture/hash-password (fn [_ _] "hashed_password")
                    ]
        (let [request {:body-params {:email "existinguser@example.com"
                                   :name "Existing User"
                                   :password "Valid1!password"}
                      :user-use-case user-use-case}
              response (user/register request)]
          (is (= 409 (:status response)))
          (is (= "이미 존재하는 이메일입니다" (get-in response [:body :error]))))))

    (testing "유효하지 않은 이메일로 회원 등록 시도" 
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)
                    id-generator-fixture/generate-ulid (fn [_] "generated_ulid")
                    password-gateway-fixture/hash-password (fn [_ _] "hashed_password")]
        (let [request {:body-params {:email "invalid-email"
                                   :name "Invalid Email User"
                                   :password "Valid1!password"}
                      :user-use-case user-use-case}
              response (user/register request)]
          (is (= 400 (:status response)))
          (is (= "유효하지 않은 이메일입니다" (get-in response [:body :error]))))))

    (testing "유효하지 않은 비밀번호로 회원 등록 시도"
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)
                    id-generator-fixture/generate-ulid (fn [_] "generated_ulid")
                    password-gateway-fixture/hash-password (fn [_ _] "hashed_password")] 
        (let [request {:body-params {:email "newuser@example.com"
                                     :name "New User"
                                     :password "short"}
                       :user-use-case user-use-case}
              response (user/register request)]
          (is (= 400 (:status response)))
          (is (= "유효하지 않은 비밀번호입니다" (get-in response [:body :error]))))))))

(deftest authenticate-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        id-generator (->TestIdGenerator)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)]

    (testing "유효한 인증 정보로 로그인"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    password-gateway-fixture/verify-password (fn [_ _ _] true)
                    token-gateway-fixture/generate (fn [_ _ _] "valid_token")]
        (let [request {:body-params {:email "test@example.com"
                                   :password "Valid1!password"}
                      :user-use-case user-use-case}
              response (user/authenticate request)]
          (is (= 200 (:status response)))
          (is (= "valid_token" (get-in response [:body :token]))))))

    (testing "존재하지 않는 이메일로 로그인 시도"
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)]
        (let [request {:body-params {:email "nonexistent@example.com"
                                   :password "Valid1!password"}
                      :user-use-case user-use-case}
              response (user/authenticate request)]
          (is (= 404 (:status response)))
          (is (= "사용자를 찾을 수 없습니다" (get-in response [:body :error]))))))

    (testing "잘못된 비밀번호로 로그인 시도"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :email "test@example.com"
                       :hashed-password "hashed_password"})
                    password-gateway-fixture/verify-password (fn [_ _ _] false)
                    token-gateway-fixture/generate (fn [_ _ _] "valid_token")]
        (let [request {:body-params {:email "test@example.com"
                                   :password "WrongPassword1!"}
                      :user-use-case user-use-case}
              response (user/authenticate request)]
          (is (= 401 (:status response)))
          (is (= "이메일 또는 비밀번호가 일치하지 않습니다" (get-in response [:body :error]))))))

    (testing "탈퇴한 사용자로 로그인 시도"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :email "test@example.com"
                       :hashed-password "hashed_password"
                       :deleted-at (java.util.Date.)})
                    password-gateway-fixture/verify-password (fn [_ _ _] true)
                    token-gateway-fixture/generate (fn [_ _ _] "valid_token")]
        (let [request {:body-params {:email "test@example.com"
                                   :password "Valid1!password"}
                      :user-use-case user-use-case}
              response (user/authenticate request)]
          (is (= 403 (:status response)))
          (is (= "탈퇴한 사용자입니다" (get-in response [:body :error]))))))))

(deftest update-profile-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        id-generator (->TestIdGenerator)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)
        test-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        auth-user {:uuid test-uuid
                   :email "test@example.com"
                   :token "valid-token"}]

    (testing "유효한 정보로 프로필 업데이트"
      (with-redefs [token-gateway-fixture/verify (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :name "Test User"})
                    user-repository-fixture/find-by-email (fn [_ _] nil)
                    user-repository-fixture/save! (fn [_ user] user)]
        (let [request {:body-params {:name "New Name"
                                     :email "new@example.com"}
                       :user-use-case user-use-case
                       :auth-user auth-user}
              response (user/update-profile request)]
          (is (= 200 (:status response)))
          (is (= test-uuid (get-in response [:body :user-uuid])))
          (is (= "new@example.com" (get-in response [:body :email])))
          (is (= "New Name" (get-in response [:body :name]))))))

    (testing "이미 존재하는 이메일로 업데이트 시도"
      (with-redefs [token-gateway-fixture/verify (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :name "Test User"})
                    user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :email "existing@example.com"})]
        (let [request {:body-params {:email "existing@example.com"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/update-profile request)]
          (is (= 409 (:status response)))
          (is (= "이미 존재하는 이메일입니다" (get-in response [:body :error]))))))

    (testing "존재하지 않는 사용자"
      (with-redefs [token-gateway-fixture/verify (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id (fn [_ _] nil)]
        (let [request {:body-params {:name "New Name"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/update-profile request)]
          (is (= 404 (:status response)))
          (is (= "사용자를 찾을 수 없습니다" (get-in response [:body :error]))))))

    (testing "탈퇴한 사용자"
      (with-redefs [token-gateway-fixture/verify (fn [_ _] test-uuid)
                    user-repository-fixture/find-by-id
                    (fn [_ _] 
                      {:uuid test-uuid
                       :email "test@example.com"
                       :name "Test User"
                       :deleted-at (java.util.Date.)})]
        (let [request {:body-params {:name "New Name"}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/update-profile request)]
          (is (= 400 (:status response)))
          (is (= "탈퇴한 사용자입니다" (get-in response [:body :error]))))))))

(deftest update-user-role-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        id-generator (->TestIdGenerator)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)
        admin-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
        auth-user {:uuid admin-uuid
                  :email "admin@example.com"}]

    (testing "관리자가 유효한 사용자 역할 업데이트"
      (with-redefs [user-repository-fixture/find-id-by-uuid 
                    (fn [_ uuid]
                      (if (= uuid admin-uuid) 1 2))
                    user-repository-fixture/save! (fn [_ user] user)
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
                           :roles #{:user}}))]
        (let [request {:body-params {:role :moderator}
                      :path-params {:id (str user-uuid)}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/update-user-role request)]
          (is (= 200 (:status response)))
          (is (= user-uuid (get-in response [:body :user-uuid])))
          (is (= #{:moderator} (get-in response [:body :roles]))))))

    (testing "관리자가 아닌 사용자의 역할 업데이트 시도"
      (with-redefs [user-repository-fixture/find-id-by-uuid (fn [_ _] 1)
                    user-repository-fixture/find-by-id
                    (fn [_ _] 
                      {:id 1
                       :uuid admin-uuid
                       :email "user@example.com"
                       :roles #{:user}})]
        (let [request {:body-params {:role :admin}
                      :path-params {:id (str user-uuid)}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/update-user-role request)]
          (is (= 403 (:status response)))
          (is (= "권한이 부족합니다" (get-in response [:body :error]))))))

    (testing "존재하지 않는 관리자의 역할 업데이트 시도"
      (with-redefs [user-repository-fixture/find-id-by-uuid (fn [_ _] nil)]
        (let [request {:body-params {:role :admin}
                      :path-params {:id (str user-uuid)}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/update-user-role request)]
          (is (= 404 (:status response)))
          (is (= "관리자를 찾을 수 없습니다" (get-in response [:body :error]))))))

    (testing "탈퇴한 사용자 역할 업데이트"
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
        (let [request {:body-params {:role :admin}
                      :path-params {:id (str user-uuid)}
                      :user-use-case user-use-case
                      :auth-user auth-user}
              response (user/update-user-role request)]
          (is (= 400 (:status response)))
          (is (= "탈퇴한 사용자입니다" (get-in response [:body :error]))))))))

(deftest request-email-verification-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        id-generator (->TestIdGenerator)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)]

    (testing "유효한 이메일로 인증 요청"
      (with-redefs [user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:email "test@example.com"
                       :name "Test User"})
                    email-token-gateway-fixture/generate-token
                    (fn [_ _ _] "test-token")
                    email-gateway-fixture/send-verification-email
                    (fn [_ _ _] nil)
                    email-verification-gateway-fixture/save-verification-status!
                    (fn [_ _ _ _] nil)]
        (let [request {:body-params {:email "test@example.com"}
                      :user-use-case user-use-case}
              response (user/request-email-verification request)]
          (is (= 200 (:status response)))
          (is (= "이메일 인증 링크가 발송되었습니다" (get-in response [:body :message]))))))

    (testing "존재하지 않는 이메일로 인증 요청"
      (with-redefs [user-repository-fixture/find-by-email (fn [_ _] nil)
                    email-token-gateway-fixture/generate-token
                    (fn [_ _ _] "test-token")
                    email-gateway-fixture/send-verification-email
                    (fn [_ _ _] nil)
                    email-verification-gateway-fixture/save-verification-status!
                    (fn [_ _ _ _] nil)]
        (let [request {:body-params {:email "nonexistent@example.com"}
                      :user-use-case user-use-case}
              response (user/request-email-verification request)]
          (is (= 404 (:status response)))
          (is (= "사용자를 찾을 수 없습니다" (get-in response [:body :error]))))))))

(deftest verify-email-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        event-subscriber (->TestEventSubscriber)
        email-gateway (->TestEmailGateway)
        id-generator (->TestIdGenerator)
        email-token-gateway (->TestEmailTokenGateway)
        email-verification-gateway (->TestEmailVerificationGateway)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway id-generator)]

    (testing "유효한 토큰으로 이메일 인증"
      (with-redefs [email-token-gateway-fixture/verify-token
                    (fn [_ _] {:email "test@example.com"})
                    user-repository-fixture/find-by-email
                    (fn [_ _] 
                      {:email "test@example.com"
                       :name "Test User"})
                    email-verification-gateway-fixture/save-verification-status!
                    (fn [_ _ _ _] nil)]
        (let [request {:body-params {:token "valid-token"}
                      :user-use-case user-use-case}
              response (user/verify-email request)]
          (is (= 200 (:status response)))
          (is (= "이메일 인증이 완료되었습니다" (get-in response [:body :message]))))))

    (testing "유효하지 않은 토큰으로 인증 시도"
      (with-redefs [email-token-gateway-fixture/verify-token
                    (fn [_ _] (f/fail :verification-error/invalid-token))]
        (let [request {:body-params {:token "invalid-token"}
                      :user-use-case user-use-case}
              response (user/verify-email request)]
          (is (= 400 (:status response)))
          (is (= "유효하지 않은 토큰입니다" (get-in response [:body :error]))))))))

