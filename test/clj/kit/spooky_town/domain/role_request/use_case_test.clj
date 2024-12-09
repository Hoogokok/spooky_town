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
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        role-request-repository (test-role-request-repository/->TestRoleRequestRepository)
        user-repository (test-user-repository/->TestUserRepository)
        user-role-repository (test-user-role-repository/->TestUserRoleRepository)
        role-repository (test-role-repository/->TestRoleRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        use-case (use-case/->RoleRequestUseCaseImpl 
                  with-tx 
                  role-request-repository 
                  user-repository 
                  user-role-repository 
                  role-repository 
                  event-publisher)]

    (testing "유효한 역할 변경 요청"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "user-01")
                    test-user-role-repository/find-roles-by-user (fn [_ _] [{:role-id "role-01"}])
                    test-role-repository/find-by-id (fn [_ _] {:role-id "role-01" :role-name :user})
                    test-role-request-repository/save! (fn [_ request] 
                                                       (assoc request 
                                                              :id 1
                                                              :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"))]
        (let [result (use-case/request-role-change 
                      use-case 
                      {:user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :role :content-creator
                       :reason "콘텐츠 크리에이터가 되고 싶습니다"})]
          (is (not (f/failed? result)))
          (is (= :content-creator (:requested-role result)))
          (is (= :user (:current-role result)))
          (is (= :pending (:status result))))))

    (testing "존재하지 않는 사용자"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] nil)]
        (let [result (use-case/request-role-change 
                      use-case 
                      {:user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :role :content-creator
                       :reason "콘텐츠 크리에이터가 되고 싶습니다"})]
          (is (f/failed? result))
          (is (= :user/not-found (f/message result))))))

    (testing "사용자 역할을 찾을 수 없음"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "user-01")
                    test-user-role-repository/find-roles-by-user (fn [_ _] [])]
        (let [result (use-case/request-role-change 
                      use-case 
                      {:user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
                       :role :content-creator
                       :reason "콘텐츠 크리에이터가 되고 싶습니다"})]
          (is (f/failed? result))
          (is (= :user/role-not-found (f/message result))))))))

(deftest approve-role-request-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        role-request-repository (test-role-request-repository/->TestRoleRequestRepository)
        user-repository (test-user-repository/->TestUserRepository)
        user-role-repository (test-user-role-repository/->TestUserRoleRepository)
        role-repository (test-role-repository/->TestRoleRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        use-case (use-case/->RoleRequestUseCaseImpl 
                  with-tx 
                  role-request-repository 
                  user-repository 
                  user-role-repository 
                  role-repository 
                  event-publisher)]

    (testing "유효한 승인 요청"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "admin-01")
                    test-role-request-repository/find-id-by-uuid (fn [_ _] "request-01")
                    test-role-request-repository/find-by-id (fn [_ _] 
                                                            {:id "request-01"
                                                             :user-id "user-01"
                                                             :current-role :user
                                                             :requested-role :content-creator
                                                             :status :pending})
                    test-role-request-repository/update-request (fn [_ request] request)
                    test-publisher/publish (fn [_ event-type event-data] nil)]
        (let [result (use-case/approve-role-request
                      use-case
                      {:admin-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"})]
          (is (not (f/failed? result)))
          (is (= :approved (:status result)))
          (is (= "admin-01" (:approved-by result))))))

    (testing "존재하지 않는 관리자"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] nil)]
        (let [result (use-case/approve-role-request
                      use-case
                      {:admin-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"})]
          (is (f/failed? result))
          (is (= :user/not-found (f/message result))))))

    (testing "존재하지 않는 요청"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "admin-01")
                    test-role-request-repository/find-id-by-uuid (fn [_ _] nil)]
        (let [result (use-case/approve-role-request
                      use-case
                      {:admin-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"})]
          (is (f/failed? result))
          (is (= :role-request/not-found (f/message result))))))

    (testing "이미 처리된 요청"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "admin-01")
                    test-role-request-repository/find-id-by-uuid (fn [_ _] "request-01")
                    test-role-request-repository/find-by-id (fn [_ _] 
                                                            {:id "request-01"
                                                             :user-id "user-01"
                                                             :current-role :user
                                                             :requested-role :content-creator
                                                             :status :approved
                                                             :approved-by "other-admin"})]
        (let [result (use-case/approve-role-request
                      use-case
                      {:admin-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"})]
          (is (f/failed? result))
          (is (= :role-request/invalid-status (f/message result))))))))

(deftest reject-role-request-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        role-request-repository (test-role-request-repository/->TestRoleRequestRepository)
        user-repository (test-user-repository/->TestUserRepository)
        user-role-repository (test-user-role-repository/->TestUserRoleRepository)
        role-repository (test-role-repository/->TestRoleRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        use-case (use-case/->RoleRequestUseCaseImpl 
                  with-tx 
                  role-request-repository 
                  user-repository 
                  user-role-repository 
                  role-repository 
                  event-publisher)]

    (testing "유효한 거절 요청"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "admin-01")
                    test-role-request-repository/find-id-by-uuid (fn [_ _] "request-01")
                    test-role-request-repository/find-by-id (fn [_ _] 
                                                            {:id "request-01"
                                                             :user-id "user-01"
                                                             :current-role :user
                                                             :requested-role :content-creator
                                                             :status :pending})
                    test-role-request-repository/update-request (fn [_ request] request)
                    test-publisher/publish (fn [_ event-type event-data] nil)]
        (let [result (use-case/reject-role-request
                      use-case
                      {:admin-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"
                       :reason "자격 요건이 충분하지 않습니다"})]
          (is (not (f/failed? result)))
          (is (= :rejected (:status result)))
          (is (= "admin-01" (:rejected-by result)))
          (is (= "자격 요건이 충분하지 않습니다" (:rejection-reason result))))))

    (testing "존재하지 않는 관리자"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] nil)]
        (let [result (use-case/reject-role-request
                      use-case
                      {:admin-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"
                       :reason "자격 요건이 충분하지 않습니다"})]
          (is (f/failed? result))
          (is (= :user/not-found (f/message result))))))

    (testing "존재하지 않는 요청"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "admin-01")
                    test-role-request-repository/find-id-by-uuid (fn [_ _] nil)]
        (let [result (use-case/reject-role-request
                      use-case
                      {:admin-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"
                       :reason "자격 요건이 충분하지 않습니다"})]
          (is (f/failed? result))
          (is (= :role-request/not-found (f/message result))))))

    (testing "이미 처리된 요청"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "admin-01")
                    test-role-request-repository/find-id-by-uuid (fn [_ _] "request-01")
                    test-role-request-repository/find-by-id (fn [_ _] 
                                                            {:id "request-01"
                                                             :user-id "user-01"
                                                             :current-role :user
                                                             :requested-role :content-creator
                                                             :status :approved
                                                             :approved-by "other-admin"})]
        (let [result (use-case/reject-role-request
                      use-case
                      {:admin-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
                       :request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"
                       :reason "자격 요건이 충분하지 않습니다"})]
          (is (f/failed? result))
          (is (= :role-request/invalid-status (f/message result))))))))

(deftest get-user-requests-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        role-request-repository (test-role-request-repository/->TestRoleRequestRepository)
        user-repository (test-user-repository/->TestUserRepository)
        user-role-repository (test-user-role-repository/->TestUserRoleRepository)
        role-repository (test-role-repository/->TestRoleRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        use-case (use-case/->RoleRequestUseCaseImpl 
                  with-tx 
                  role-request-repository 
                  user-repository 
                  user-role-repository 
                  role-repository 
                  event-publisher)]

    (testing "사용자의 요청 목록 조회"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] "user-01")
                    test-role-request-repository/find-all-by-user (fn [_ user-id]
                                                                  [{:id "request-01"
                                                                    :uuid #uuid "880e8400-e29b-41d4-a716-446655440000"
                                                                    :user-id user-id
                                                                    :current-role :user
                                                                    :requested-role :content-creator
                                                                    :status :pending}
                                                                   {:id "request-02"
                                                                    :uuid #uuid "990e8400-e29b-41d4-a716-446655440000"
                                                                    :user-id user-id
                                                                    :current-role :user
                                                                    :requested-role :admin
                                                                    :status :rejected
                                                                    :rejected-by "admin-01"
                                                                    :rejection-reason "자격 요건이 충분하지 않습니다"}])]
        (let [result (use-case/get-user-requests
                      use-case
                      {:user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"})]
          (is (not (f/failed? result)))
          (is (= 2 (count result)))
          (is (= :pending (:status (first result))))
          (is (= :rejected (:status (second result)))))))

    (testing "존재하지 않는 사용자의 요청 조회"
      (with-redefs [test-user-repository/find-id-by-uuid (fn [_ _] nil)]
        (let [result (use-case/get-user-requests
                      use-case
                      {:user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"})]
          (is (f/failed? result))
          (is (= :user/not-found (f/message result))))))))

(deftest get-pending-requests-test
  (let [with-tx (fn [repos f]
                  (case (count repos)
                    1 (f (first repos))
                    2 (f (first repos) (second repos))
                    4 (f (first repos) (second repos) (nth repos 2) (nth repos 3))
                    (throw (ex-info "Invalid number of repositories" {:repos-count (count repos)}))))
        role-request-repository (test-role-request-repository/->TestRoleRequestRepository)
        user-repository (test-user-repository/->TestUserRepository)
        user-role-repository (test-user-role-repository/->TestUserRoleRepository)
        role-repository (test-role-repository/->TestRoleRepository)
        event-publisher (test-publisher/->TestEventPublisher nil)
        use-case (use-case/->RoleRequestUseCaseImpl 
                  with-tx 
                  role-request-repository 
                  user-repository 
                  user-role-repository 
                  role-repository 
                  event-publisher)]

    (testing "대기 중인 요청 목록 조회"
      (with-redefs [test-role-request-repository/find-all-pending 
                    (fn [_]
                      [{:id "request-01"
                        :uuid #uuid "880e8400-e29b-41d4-a716-446655440000"
                        :user-id "user-01"
                        :current-role :user
                        :requested-role :content-creator
                        :status :pending}
                       {:id "request-02"
                        :uuid #uuid "990e8400-e29b-41d4-a716-446655440000"
                        :user-id "user-02"
                        :current-role :user
                        :requested-role :content-creator
                        :status :pending}])]
        (let [result (use-case/get-pending-requests use-case)]
          (is (not (f/failed? result)))
          (is (= 2 (count result)))
          (is (every? #(= :pending (:status %)) result))
          (is (every? #(= :content-creator (:requested-role %)) result)))))

    (testing "대기 중인 요청이 없는 경우"
      (with-redefs [test-role-request-repository/find-all-pending (fn [_] [])]
        (let [result (use-case/get-pending-requests use-case)]
          (is (not (f/failed? result)))
          (is (empty? result)))))))

