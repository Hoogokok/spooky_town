(ns kit.spooky-town.domain.role-request.use-case-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.role-request.use-case :as use-case]
            [kit.spooky-town.domain.role-request.test.repository :as test-repository]
            [failjure.core :as f]))

(deftest request-role-change-test
  (let [with-tx (fn [f] (f nil))
        role-request-repository (test-repository/->TestRoleRequestRepository)
        role-request-use-case (use-case/->RoleRequestUseCaseImpl with-tx role-request-repository)]

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