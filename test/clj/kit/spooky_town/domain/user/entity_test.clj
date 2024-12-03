(ns kit.spooky-town.domain.user.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.user.entity :as entity]))

(deftest create-user-test
  (testing "유효한 데이터로 User 생성"
    (let [user (entity/create-user
                {:uuid #uuid "00000000-0000-0000-0000-000000000000"
                 :email "test@example.com"
                 :name "Test User"
                 :hashed-password "hashed_password_123"
                 :roles #{:user}})]
      (is (some? user))
      (is (= "test@example.com" (:email user)))
      (is (= "Test User" (:name user)))
      (is (= #{:user} (:roles user))))))

(deftest mark-as-withdrawn-test
  (testing "사용자 탈퇴 처리"
    (let [user (entity/create-user
                {:uuid #uuid "00000000-0000-0000-0000-000000000000"
                 :email "test@example.com"
                 :name "Test User"
                 :hashed-password "hashed_password_123"
                 :roles #{:user}})
          reason "테스트 탈퇴"
          withdrawn-user (entity/mark-as-withdrawn user reason)]
      (is (some? (:deleted-at withdrawn-user)))
      (is (= reason (:withdrawal-reason withdrawn-user)))
      (is (entity/withdrawn? withdrawn-user))))
  
  (testing "탈퇴 여부 확인"
    (let [active-user (entity/create-user
                       {:uuid #uuid "00000000-0000-0000-0000-000000000000"
                        :email "test@example.com"
                        :name "Test User"
                        :hashed-password "hashed_password_123"
                        :roles #{:user}})
          withdrawn-user (entity/create-user
                         {:uuid #uuid "00000000-0000-0000-0000-000000000000"
                          :email "test@example.com"
                          :name "Test User"
                          :hashed-password "hashed_password_123"
                          :roles #{:user}
                          :deleted-at (java.util.Date.)
                          :withdrawal-reason "탈퇴 사유"})]
      (is (not (entity/withdrawn? active-user)))
      (is (entity/withdrawn? withdrawn-user)))))

