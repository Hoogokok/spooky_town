(ns kit.spooky-town.domain.role-request.use-case-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.role-request.use-case :as use-case]
            [kit.spooky-town.domain.role-request.test.repository :as test-repository]
            [kit.spooky-town.domain.event.test.publisher :as test-publisher]
            [failjure.core :as f]))

(deftest request-role-change-test
  (let [with-tx (fn [f] (f nil))
        role-request-repository (test-repository/->TestRoleRequestRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        role-request-use-case (use-case/->RoleRequestUseCaseImpl with-tx role-request-repository event-publisher)]

    (testing "유효한 역할 변경 요청"
      (with-redefs [test-repository/save! (fn [_ request] request)]
        (let [result (use-case/request-role-change 
                      role-request-use-case 
                      {:user-id 1
                       :role :moderator
                       :reason "저는 커뮤니티를 도와드리고 1년 이상 활동했습니다. 그러니 이제 관리자 권한을 받아도 괜찮을 것 같습니다."})]
          (is (not (f/failed? result)))
          (is (uuid? (:uuid result)))
          (is (= :moderator (:requested-role result)))
          (is (= :pending (:status result))))))
    
    (testing "유효하지 않은 요청"
      (with-redefs [test-repository/save! (fn [_ request] request)]
        (let [result (use-case/request-role-change 
                      role-request-use-case 
                      {:user-id 1
                       :role :moderator
                       :reason "내놔"})]  ;; 10자 미만
          (is (f/failed? result))
          (is (= :role-request/invalid-request (f/message result))))))))

(deftest approve-role-request-test
  (let [with-tx (fn [f] (f nil))
        role-request-repository (test-repository/->TestRoleRequestRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        role-request-use-case (use-case/->RoleRequestUseCaseImpl with-tx role-request-repository event-publisher)
        pending-request {:id 1
                        :uuid (random-uuid)
                        :user-id 1
                        :requested-role :moderator
                        :reason "저는 커뮤니티를 도와드리고 1년 이상 활동했습니다."
                        :status :pending
                        :created-at (java.util.Date.)}]
    
    (testing "대기 중인 요청 승인"
      (with-redefs [test-repository/find-by-id (fn [_ _] pending-request)
                    test-repository/update-request (fn [_ request] request)
                    test-publisher/publish (fn [_ event-type payload]
                                           (is (= :role-request/approved event-type))
                                           (is (= {:user-id 1 :role :moderator} payload)))]
        (let [result (use-case/approve-role-request
                      role-request-use-case
                      {:admin-id 2
                       :request-id 1})]
          (is (not (f/failed? result)))
          (is (= :approved (:status result)))
          (is (= 2 (:approved-by result))))))
    
    (testing "존재하지 않는 요청"
      (with-redefs [test-repository/find-by-id (fn [_ _] nil)]
        (let [result (use-case/approve-role-request
                      role-request-use-case
                      {:admin-id 2
                       :request-id 999})]
          (is (f/failed? result))
          (is (= :role-request/not-found (f/message result))))))))

(deftest reject-role-request-test
  (let [with-tx (fn [f] (f nil))
        role-request-repository (test-repository/->TestRoleRequestRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        role-request-use-case (use-case/->RoleRequestUseCaseImpl with-tx role-request-repository event-publisher)
        pending-request {:id 1
                        :uuid (random-uuid)
                        :user-id 1
                        :requested-role :moderator
                        :reason "저는 커뮤니티를 도와드리고 1년 이상 활동했습니다."
                        :status :pending
                        :created-at (java.util.Date.)}]
    
    (testing "대기 중인 요청 거절"
      (with-redefs [test-repository/find-by-id (fn [_ _] pending-request)
                    test-repository/update-request (fn [_ request] request)]
        (let [result (use-case/reject-role-request
                      role-request-use-case
                      {:admin-id 2
                       :request-id 1
                       :reason "커뮤니티 활동이 아직 충분하지 않습니다. 6개월 후에 다시 신청해주세요."})]
          (is (not (f/failed? result)))
          (is (= :rejected (:status result)))
          (is (= 2 (:rejected-by result)))
          (is (string? (:rejection-reason result))))))
    
    (testing "존재하지 않는 요청"
      (with-redefs [test-repository/find-by-id (fn [_ _] nil)]
        (let [result (use-case/reject-role-request
                      role-request-use-case
                      {:admin-id 2
                       :request-id 999
                       :reason "해당 요청을 찾을 수 없습니다."})]
          (is (f/failed? result))
          (is (= :role-request/not-found (f/message result)))))))) 