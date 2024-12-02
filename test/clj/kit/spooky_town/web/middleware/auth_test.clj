(ns kit.spooky-town.web.middleware.auth-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.web.middleware.auth :as auth]
            [kit.spooky-town.domain.user.gateway.token :as token]
            [failjure.core :as f]))

(defrecord MockTokenGateway [verify-fn]
  token/TokenGateway
  (generate [_ _ _] "mock-token")
  (verify [_ token] (verify-fn token))
  (revoke-token [_ _] true))

(defn- make-request
  "테스트용 요청 생성"
  [token]
  {:headers {"authorization" (str "Bearer " token)}})

(defn- success-handler
  "성공 응답을 반환하는 핸들러"
  [request]
  {:status 200
   :body (:identity request)})

(deftest wrap-auth-test
  (testing "토큰이 없는 요청"
    (let [handler (auth/wrap-auth success-handler (->MockTokenGateway nil))
          response (handler {:headers {}})]
      (is (= 200 (:status response)))
      (is (nil? (get-in response [:body :identity])))))

  (testing "유효한 토큰"
    (let [user-data 1  ;; user-id 반환
          gateway (->MockTokenGateway (fn [_] user-data))
          handler (auth/wrap-auth success-handler gateway)
          response (handler (make-request "valid-token"))]
      (is (= 200 (:status response)))
      (is (= user-data (:body response)))))

  (testing "유효하지 않은 토큰"
    (let [gateway (->MockTokenGateway (fn [_] (f/fail :token-error/invalid)))
          handler (auth/wrap-auth success-handler gateway)
          response (handler (make-request "invalid-token"))]
      (is (= 401 (:status response)))
      (is (= :token-error/invalid (get-in response [:body :message]))))))

(deftest wrap-auth-required-test
  (testing "인증된 요청"
    (let [handler (auth/wrap-auth-required success-handler)
          response (handler {:identity {:email "test@example.com"}})]
      (is (= 200 (:status response)))))

  (testing "인증되지 않은 요청"
    (let [handler (auth/wrap-auth-required success-handler)
          response (handler {})]
      (is (= 401 (:status response)))
      (is (= "Authentication required" (get-in response [:body :message]))))))