(ns kit.spooky-town.domain.role-request.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.role-request.entity :as entity]))

(deftest create-role-request-test
  (testing "유효한 역할 변경 요청 생성"
    (let [request (entity/create-role-request
                   {:user-id 1
                    :requested-role :moderator
                    :reason "I want to help moderate the community and have been active for over a year."})]
      (is (uuid? (:uuid request)))
      (is (= 1 (:user-id request)))
      (is (= :moderator (:requested-role request)))
      (is (= :pending (:status request)))
      (is (inst? (:created-at request)))))
  
  (testing "유효하지 않은 요청은 nil 반환"
    (is (nil? (entity/create-role-request
                {:user-id 1
                 :requested-role :moderator
                 :reason "short"})))  ;; 10자 미만
    (is (nil? (entity/create-role-request
                {:user-id nil
                 :requested-role :moderator
                 :reason "valid reason here"})))))

(deftest approve-request-test
  (testing "대기 중인 요청 승인"
    (let [request (entity/create-role-request
                   {:user-id 1
                    :requested-role :moderator
                    :reason "I want to help moderate the community."})
          approved (entity/approve-request request 2)]  ;; admin-id = 2
      (is (= :approved (:status approved)))
      (is (= 2 (:approved-by approved)))
      (is (inst? (:updated-at approved)))))
  
  (testing "이미 처리된 요청은 승인 불가"
    (let [request (-> (entity/create-role-request
                       {:user-id 1
                        :requested-role :moderator
                        :reason "I want to help moderate the community."})
                     (entity/approve-request 2))]
      (is (nil? (entity/approve-request request 3))))))

(deftest reject-request-test
  (testing "대기 중인 요청 거절"
    (let [request (entity/create-role-request
                   {:user-id 1
                    :requested-role :moderator
                    :reason "I want to help moderate the community."})
          rejected (entity/reject-request 
                    request 
                    2 
                    "Not enough community participation yet.")]
      (is (= :rejected (:status rejected)))
      (is (= 2 (:rejected-by rejected)))
      (is (= "Not enough community participation yet." (:rejection-reason rejected)))
      (is (inst? (:updated-at rejected)))))
  
  (testing "거절 사유가 너무 짧으면 거절 불가"
    (let [request (entity/create-role-request
                   {:user-id 1
                    :requested-role :moderator
                    :reason "I want to help moderate the community."})]
      (is (nil? (entity/reject-request request 2 "short"))))))

(deftest status-check-test
  (let [request (entity/create-role-request
                 {:user-id 1
                  :requested-role :moderator
                  :reason "I want to help moderate the community."})]
    
    (testing "초기 상태는 pending"
      (is (entity/pending? request))
      (is (not (entity/approved? request)))
      (is (not (entity/rejected? request))))
    
    (testing "승인된 요청"
      (let [approved (entity/approve-request request 2)]
        (is (not (entity/pending? approved)))
        (is (entity/approved? approved))
        (is (not (entity/rejected? approved)))))
    
    (testing "거절된 요청"
      (let [rejected (entity/reject-request request 2 "Not enough experience.")]
        (is (not (entity/pending? rejected)))
        (is (not (entity/approved? rejected)))
        (is (entity/rejected? rejected)))))) 