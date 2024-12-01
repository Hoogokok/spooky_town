(ns kit.spooky-town.domain.user.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.user.entity :as entity]))

(deftest create-user-test
  (testing "유효한 데이터로 User 생성"
    (let [now (java.util.Date.)
          user (entity/create-user
                {:uuid #uuid "00000000-0000-0000-0000-000000000000"
                 :email "test@example.com"
                 :name "Test User"
                 :hashed-password "hashed_password_123"
                 :created-at now})]
      (is (some? user))
      (is (= "test@example.com" (:email user)))
      (is (= "Test User" (:name user)))
      (is (= #{:user} (:roles user)))))

  (testing "필수 필드 누락 시 생성 실패"
    (is (nil? (entity/create-user {})))
    (is (nil? (entity/create-user {:email "test@example.com"})))
    (is (nil? (entity/create-user {:name "Test User"})))))