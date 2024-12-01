(ns kit.spooky-town.infrastructure.auth.jwt-gateway-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.infrastructure.auth.jwt-gateway :as jwt-gateway]
            [kit.spooky-town.domain.auth.gateway :as gateway]
            [failjure.core :as f]))

(def test-config
  {:jwt-secret "test-secret-key"
   :token-expire-hours 1})

(deftest jwt-gateway-test
  (let [gateway (jwt-gateway/->JWTGateway test-config)
        user-data {:email "test@example.com" :roles #{:user}}]
    
    (testing "토큰 생성"
      (let [token (gateway/create-token gateway user-data)]
        (is (not (f/failed? token)))))
    
    (testing "토큰 검증"
      (let [token (gateway/create-token gateway user-data)
            result (gateway/verify-token gateway token)]
        (is (not (f/failed? result)))
        (is (= user-data result))))
    
    (testing "잘못된 토큰 검증"
      (let [result (gateway/verify-token gateway "invalid-token")]
        (is (f/failed? result)))))) 