(ns kit.spooky-town.domain.role-request.use-case-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.role-request.use-case :as use-case]
            [kit.spooky-town.domain.role-request.test.repository :as test-role-request-repository]
            [kit.spooky-town.domain.role.test.repository :as test-role-repository]
            [kit.spooky-town.domain.user.test.repository :as test-user-repository]
            [kit.spooky-town.domain.user-role.test.repository :as test-user-role-repository]
            [kit.spooky-town.domain.event.test.publisher :as test-publisher]
            [failjure.core :as f]))

(deftest request-role-change-test
  (let [with-tx (fn [repos f]
                  (let [role-request-repo (first repos)
                        user-repo (second repos)
                        user-role-repo (nth repos 2)
                        role-repo (nth repos 3)]
                    (f role-request-repo user-repo user-role-repo role-repo)))
        role-request-repository (test-role-request-repository/->TestRoleRequestRepository)
        user-repository (test-user-repository/->TestUserRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        role-repository (test-role-repository/->TestRoleRepository)
        user-role-repository (test-user-role-repository/->TestUserRoleRepository)
        role-request-use-case (use-case/->RoleRequestUseCaseImpl 
                              with-tx 
                              role-request-repository 
                              user-repository 
                              user-role-repository 
                              role-repository 
                              event-publisher)
        request-uuid (java.util.UUID/randomUUID)]

    (testing "유효한 역할 변경 요청"
      (with-redefs [test-role-request-repository/save!
                    (fn [_ request]
                      {:id 1
                       :uuid request-uuid
                       :user-id "user-01HQ..."
                       :current-role :user
                       :requested-role :content-creator
                       :reason "나는 콘텐츠 크리에이터가 되고 싶어"
                       :status :pending})
                    test-user-repository/find-id-by-uuid (fn [_ _] "user-01HQ...")
                    test-user-role-repository/find-roles-by-user (fn [_ _] [{:role-id "role-01HQ..."}])
                    test-role-repository/find-by-id (fn [_ _] {:role-id "role-01HQ..." :role-name :user})
                    test-role-request-repository/find-id-by-uuid (fn [_ _] nil)
                    test-role-request-repository/find-by-id (fn [_ _] nil)
                    test-role-request-repository/update-request (fn [_ request] request)
                    test-publisher/publish (fn [_ _ _] nil)]
        (let [result (use-case/request-role-change 
                      role-request-use-case 
                      {:user-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :role :content-creator
                       :reason "나는 콘텐츠 크리에이터가 되고 싶어"})]

          (is (not (f/failed? result)))
          (is (uuid? (:uuid result)))
          (is (= :user (:current-role result)))
          (is (= :content-creator (:requested-role result)))
          (is (= :pending (:status result))))))
    
    (testing "존재하지 않는 사용자"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] nil)
                    test-role-request-repository/find-id-by-uuid (fn [_ _] nil)
                    test-role-request-repository/find-by-id (fn [_ _] nil)
                    test-role-request-repository/save! (fn [_ request] request)
                    test-role-request-repository/update-request (fn [_ request] request)
                    test-publisher/publish (fn [_ _ _] nil)]
        (let [result (use-case/request-role-change 
                      role-request-use-case 
                      {:user-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :role :content-creator
                       :reason "테스트"})]
          (is (f/failed? result))
          (is (= :user/not-found (f/message result))))))))

(deftest approve-role-request-test
  (let [with-tx (fn [repos f]
                  (let [role-request-repo (first repos)
                        user-repo (second repos)
                        user-role-repo (nth repos 2)
                        role-repo (nth repos 3)]
                    (f role-request-repo user-repo user-role-repo role-repo)))
        role-request-repository (test-role-request-repository/->TestRoleRequestRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        user-repository (test-user-repository/->TestUserRepository)
        user-role-repository (test-user-role-repository/->TestUserRoleRepository)
        role-repository (test-role-repository/->TestRoleRepository)
        role-request-use-case (use-case/->RoleRequestUseCaseImpl 
                              with-tx 
                              role-request-repository 
                              user-repository 
                              user-role-repository 
                              role-repository 
                              event-publisher)
        pending-request {:id 1
                        :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                        :user-id "user-01HQ..."
                        :current-role :user
                        :requested-role :content-creator
                        :reason "나는 콘텐츠 크리에이터가 되고 싶어"
                        :status :pending
                        :created-at (java.util.Date.)}]
    
    (testing "대기 중인 요청 승인"
      (with-redefs [test-role-request-repository/find-by-id (fn [_ _] pending-request)
                test-role-request-repository/find-id-by-uuid (fn [_ _] 1)
                test-user-repository/find-id-by-uuid (fn [_ _] 2)
                test-role-request-repository/update-request (fn [_ request] request)
                test-publisher/publish (fn [_ event-type payload]
                                     (is (= :role-request/approved event-type))
                                     (is (= {:user-id "user-01HQ..." :role :content-creator} payload)))]
        (let [result (use-case/approve-role-request
                      role-request-use-case
                      {:admin-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"})]
          (is (not (f/failed? result)))
          (is (= :approved (:status result))))))
    
    (testing "존재하지 않는 요청"
      (with-redefs [test-role-request-repository/find-id-by-uuid (fn [_ _] nil)
              test-user-repository/find-id-by-uuid (fn [_ _] 2)
              test-role-request-repository/find-by-id (fn [_ _] nil)
              test-role-request-repository/update-request (fn [_ request] request)
              test-publisher/publish (fn [_ _ _] nil)]
        (let [result (use-case/approve-role-request
                      role-request-use-case
                      {:admin-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"})]
          (is (f/failed? result))
          (is (= :role-request/not-found (f/message result))))))))

(deftest reject-role-request-test
  (let [with-tx (fn [repos f]
                  (let [role-request-repo (first repos)
                        user-repo (second repos)
                        user-role-repo (nth repos 2)
                        role-repo (nth repos 3)]
                    (f role-request-repo user-repo user-role-repo role-repo)))
        role-request-repository (test-role-request-repository/->TestRoleRequestRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        user-repository (test-user-repository/->TestUserRepository)
        user-role-repository (test-user-role-repository/->TestUserRoleRepository)
        role-repository (test-role-repository/->TestRoleRepository)
        role-request-use-case (use-case/->RoleRequestUseCaseImpl 
                              with-tx 
                              role-request-repository 
                              user-repository 
                              user-role-repository 
                              role-repository 
                              event-publisher)
        pending-request {:id 1
                        :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                        :user-id "user-01HQ..."
                        :current-role :user
                        :requested-role :content-creator
                        :reason "나는 콘텐츠 크리에이터가 되고 싶어"
                        :status :pending
                        :created-at (java.util.Date.)}]
    
    (testing "대기 중인 요청 거절"
      (with-redefs [test-role-request-repository/find-by-id (fn [_ _] pending-request)
              test-role-request-repository/find-id-by-uuid (fn [_ _] 1)
              test-user-repository/find-id-by-uuid (fn [_ _] 2)
              test-role-request-repository/update-request
              (fn [_ request]
                (assoc pending-request
                       :status :rejected
                       :rejection-reason "거절 사유는 뭐가 되든 거절이야"))
              test-publisher/publish (fn [_ _ _] nil)]
        (let [result (use-case/reject-role-request
                      role-request-use-case
                      {:admin-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :reason "거절 사유는 뭐가 되든 거절이야"})]
          (is (not (f/failed? result)))
          (is (= :rejected (:status result))))))
    
    (testing "존재하지 않는 요청"
      (with-redefs [test-role-request-repository/find-id-by-uuid (fn [_ _] nil)
              test-user-repository/find-id-by-uuid (fn [_ _] 2)
              test-role-request-repository/find-by-id (fn [_ _] nil)
              test-role-request-repository/update-request (fn [_ request] request)
              test-publisher/publish (fn [_ _ _] nil)]
        (let [result (use-case/reject-role-request
                      role-request-use-case
                      {:admin-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :reason "거절 사유는 뭐가 되든 거절이야"})]
          (is (f/failed? result))
          (is (= :role-request/not-found (f/message result)))))))) 