(ns kit.spooky-town.domain.user.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.user.entity :as entity]))

(def valid-user-data
  {:user-id "01HGD3V7XN6QX5RJQR1VVXDCP9"
   :uuid #uuid "00000000-0000-0000-0000-000000000000"
   :email "test@example.com"
   :name "Test User"
   :hashed-password "hashed_password_123"
   :created-at (java.util.Date.)})

(deftest create-user-test
  (testing "유효한 데이터로 User 생성"
    (let [user (entity/create-user valid-user-data)]
      (is (some? user))
      (is (= "01HGD3V7XN6QX5RJQR1VVXDCP9" (:user-id user)))
      (is (= "test@example.com" (:email user)))
      (is (= "Test User" (:name user)))))

  (testing "필수 필드 누락시 생성 실패"
    (is (nil? (entity/create-user (dissoc valid-user-data :email))))
    (is (nil? (entity/create-user (dissoc valid-user-data :name))))
    (is (nil? (entity/create-user (dissoc valid-user-data :hashed-password))))))

(deftest update-user-test
  (let [user (entity/create-user valid-user-data)]
    
    (testing "이메일 업데이트"
      (let [updated (entity/update-email user "new@example.com")]
        (is (some? updated))
        (is (= "new@example.com" (:email updated)))))
    
    (testing "잘못된 이메일로 업데이트 시도"
      (is (nil? (entity/update-email user "invalid-email"))))

    (testing "이름 업데이트"
      (let [updated (entity/update-name user "New Name")]
        (is (some? updated))
        (is (= "New Name" (:name updated)))))
    
    (testing "잘못된 이름으로 업데이트 시도"
      (is (nil? (entity/update-name user "A"))))  ;; 2자 미만

    (testing "비밀번호 업데이트"
      (let [updated (entity/update-password user "new_hashed_password")]
        (is (some? updated))
        (is (= "new_hashed_password" (:hashed-password updated)))))))

(deftest withdrawal-test
  (let [user (entity/create-user valid-user-data)]
    
    (testing "탈퇴 처리"
      (let [withdrawn (entity/mark-as-withdrawn user "탈퇴 사유")]
        (is (some? withdrawn))
        (is (some? (:deleted-at withdrawn)))
        (is (= "탈퇴 사유" (:withdrawal-reason withdrawn)))))
    
    (testing "탈퇴 여부 확인"
      (is (not (entity/withdrawn? user)))
      (is (entity/withdrawn? (entity/mark-as-withdrawn user "탈퇴"))))))

