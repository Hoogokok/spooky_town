(ns kit.spooky-town.application.auth.service-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.application.auth.service :as sut :refer [->AuthService]] 
            [kit.spooky-town.domain.auth.core :as auth]))

(defrecord MockAuthProvider [valid? token]
  auth/AuthenticationService
  (authenticate [_ credentials]
    (when valid?
      (auth/->AuthToken token nil)))
  (verify-token [_ token]
    (when valid?
      {:email "test@example.com"})))

(deftest auth-service-test
  (testing "성공적인 인증"
    (let [service (->AuthService
                   (->MockAuthProvider true "valid-token")
                   nil)]
      (is (some? (sut/authenticate-user service "test@example.com" "password")))
      (is (= {:email "test@example.com"}
             (sut/get-authenticated-user service "valid-token")))))

  (testing "실패한 인증"
    (let [service (->AuthService
                   (->MockAuthProvider false nil)
                   nil)]
      (is (nil? (sut/authenticate-user service "test@example.com" "wrong-password")))
      (is (nil? (sut/get-authenticated-user service "invalid-token")))))) 