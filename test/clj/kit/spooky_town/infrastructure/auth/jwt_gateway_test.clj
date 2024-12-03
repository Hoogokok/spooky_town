(ns kit.spooky-town.infrastructure.auth.jwt-gateway-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.infrastructure.auth.jwt-gateway :as jwt-gateway]
            [kit.spooky-town.domain.user.gateway.token :as token]
            [failjure.core :as f]))

(def test-config
  {:jwt-secret "test-secret-key"
   :token-expire-hours 1})

(deftest jwt-gateway-test
  (let [gateway (jwt-gateway/->JWTGateway test-config)
        user-data {:id 1 :roles #{:user}}]
    
    (testing "토큰 생성"
      (let [token (token/generate gateway user-data 1)]
        (is (not (f/failed? token)))))
    
    (testing "토큰 검증"
      (let [token (token/generate gateway user-data 1)
            result (token/verify gateway token)]
        (is (not (f/failed? result)))
        (is (= user-data result))))
    
    (testing "잘못된 토큰 검증"
      (let [result (token/verify gateway "invalid-token")]
        (is (f/failed? result)))))) 