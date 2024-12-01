(ns kit.spooky-town.domain.user.use-case-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [kit.spooky-town.domain.user.use-case :as use-case :refer [->UserUseCaseImpl]]
            [kit.spooky-town.domain.user.test.password-gateway :as password-gateway-fixture :refer [->TestPasswordGateway]]
            [kit.spooky-town.domain.user.test.token-gateway :as token-gateway-fixture :refer [->TestTokenGateway]]
            [kit.spooky-town.domain.user.test.repository :as user-repository-fixture :refer [->TestUserRepository]]))

(deftest register-user-test
  (let [with-tx (fn [repo f] (f repo))
        password-gateway (->TestPasswordGateway)
        token-gateway (->TestTokenGateway)
        user-repository (->TestUserRepository)
        user-use-case (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository)]

    (testing "유효한 데이터로 사용자 등록"
      (with-redefs [password-gateway-fixture/hash-password (fn [_ password] "hashed_password")
                    user-repository-fixture/find-by-email (fn [_ _] nil)
                    user-repository-fixture/save! (fn [_ _] true)
                    token-gateway-fixture/generate (fn [_ _ _] "generated_token")]
        (let [command {:email "test@example.com"
                       :name "Test User"
                       :password "Valid1!password"}
              result (use-case/register-user user-use-case command)]
          (println "result:" result)
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