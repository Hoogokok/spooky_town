(ns kit.spooky-town.web.controllers.role-request-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.web.controllers.role-request :as controller]
            [kit.spooky-town.domain.role-request.use-case :as use-case]
            [ring.mock.request :as mock]
            [failjure.core :as f]))

(deftest create-request-test
  (testing "일반 사용자가 역할 변경 요청 생성"
    (let [user-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
          request-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
          mock-use-case (reify use-case/RoleRequestUseCase
                         (request-role-change [_ command]
                           {:uuid request-uuid}))
          request {:role-request-use-case mock-use-case
                  :identity {:user-uuid user-uuid}
                  :body-params {:role "moderator"
                              :reason "테스트 요청입니다."}}
          response (controller/create-request request)]
      (is (= 201 (:status response)))
      (is (contains? (get-in response [:body]) :message))))

  (testing "잘못된 요청 파라미터"
    (let [user-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
          request {:identity {:user-uuid user-uuid}
                  :body-params {:role "invalid"}}
          response (controller/create-request request)]
      (is (= 400 (:status response)))
      (is (contains? (get-in response [:body]) :error)))))

(deftest get-pending-requests-test
  (testing "관리자가 대기 중인 요청 목록 조회"
    (let [request-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
          user-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
          mock-requests [{:uuid request-uuid
                         :user_uuid user-uuid
                         :requested_role "moderator"
                         :reason "테스트 요청입니다."
                         :status "pending"
                         :created_at "2024-03-21T00:00:00Z"}]
          mock-use-case (reify use-case/RoleRequestUseCase
                         (get-pending-requests [_]
                           mock-requests))
          request {:role-request-use-case mock-use-case
                  :identity {:user-uuid user-uuid
                           :roles #{:admin}}}
          response (controller/get-pending-requests request)]
      (is (= 200 (:status response)))
      (is (= mock-requests (get-in response [:body])))))

  (testing "일반 사용자의 접근 거부"
    (let [user-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
          request {:identity {:user-uuid user-uuid
                            :roles #{:user}}}
          response (controller/get-pending-requests request)]
      (is (= 403 (:status response)))
      (is (contains? (get-in response [:body]) :error)))))

(deftest reject-request-test
  (testing "관리자가 역할 변경 요청 거절"
    (let [admin-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
          request-uuid #uuid "660e8400-e29b-41d4-a716-446655440000"
          mock-use-case (reify use-case/RoleRequestUseCase
                         (reject-role-request [_ command]
                           {:uuid request-uuid
                            :status "rejected"}))
          request {:role-request-use-case mock-use-case
                  :identity {:user-uuid admin-uuid
                           :roles #{:admin}}
                  :parameters {:path {:id request-uuid}}
                  :body-params {:reason "거절 사유입니다."}}
          response (controller/reject-request request)]
      (is (= 200 (:status response)))
      (is (contains? (get-in response [:body]) :message))))

  (testing "일반 사용자의 거절 시도"
    (let [user-uuid #uuid "770e8400-e29b-41d4-a716-446655440000"
          request-uuid #uuid "880e8400-e29b-41d4-a716-446655440000"
          request {:identity {:user-uuid user-uuid
                            :roles #{:user}}
                  :parameters {:path {:id request-uuid}}
                  :body-params {:reason "거절 사유입니다."}}
          response (controller/reject-request request)]
      (is (= 403 (:status response)))
      (is (contains? (get-in response [:body]) :error))))

  (testing "거절 사유 없이 거절 시도"
    (let [admin-uuid #uuid "990e8400-e29b-41d4-a716-446655440000"
          request-uuid #uuid "aa0e8400-e29b-41d4-a716-446655440000"
          mock-use-case (reify use-case/RoleRequestUseCase
                         (reject-role-request [_ command]
                           (f/fail :role-request/invalid-reason)))
          request {:role-request-use-case mock-use-case
                  :identity {:user-uuid admin-uuid
                           :roles #{:admin}}
                  :parameters {:path {:id request-uuid}}
                  :body-params {}}
          response (controller/reject-request request)]
      (is (= 400 (:status response)))
      (is (contains? (get-in response [:body]) :error))))) 