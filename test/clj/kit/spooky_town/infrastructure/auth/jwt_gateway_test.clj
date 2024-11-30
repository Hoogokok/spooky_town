(ns kit.spooky-town.infrastructure.auth.jwt-gateway-test
  (:require [clojure.test :refer [deftest is testing]]
            [kit.spooky-town.infrastructure.auth.jwt-gateway :as sut]
            [kit.spooky-town.domain.auth.gateway :as gateway]))

(def test-config
  {:jwt-secret "test-secret"
   :token-expire-hours 24})

(deftest jwt-gateway-test
  (let [gateway (sut/create-jwt-gateway test-config)
        user-data {:id 1 :email "test@example.com"}]
    
    (testing "토큰 생성"
      (let [token (gateway/create-token gateway user-data)]
        (is (string? (:value token)))
        (is (some? (:expires-at token)))))
    
    (testing "토큰 검증"
      (let [token (gateway/create-token gateway user-data)
            verified-data (gateway/verify-token gateway (:value token))]
        (is (= user-data verified-data))))
    
    (testing "잘못된 토큰 검증"
      (is (thrown? Exception
                   (gateway/verify-token gateway "invalid-token")))))) 