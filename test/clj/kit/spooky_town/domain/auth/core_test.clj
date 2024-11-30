(ns kit.spooky-town.domain.auth.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [kit.spooky-town.domain.auth.core :as auth :refer [->Credentials ->AuthToken ->User]]))

(deftest credentials-test
  (testing "Credentials 생성"
    (let [credentials (->Credentials "test@example.com" "password123")]
      (is (= "test@example.com" (:email credentials)))
      (is (= "password123" (:password credentials)))))

  (testing "AuthToken 생성"
    (let [token (->AuthToken "token123" #inst "2024-03-21")]
      (is (= "token123" (:value token)))
      (is (= #inst "2024-03-21" (:expires-at token)))))

  (testing "User 생성"
    (let [user (->User 1 "test@example.com" #{:user})]
      (is (= 1 (:id user)))
      (is (= "test@example.com" (:email user)))
      (is (= #{:user} (:roles user)))))) 