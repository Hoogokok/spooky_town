(ns kit.spooky-town.infrastructure.auth.jwt-email-token-gateway-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.infrastructure.auth.jwt-email-token-gateway :as jwt]
            [kit.spooky-town.domain.user.gateway.email-token :as email-token-gateway]
            [failjure.core :as f]))

(deftest jwt-email-token-gateway-test
  (let [test-secret "test-secret"
        gateway (jwt/->JWTEmailTokenGateway test-secret)]
    
    (testing "토큰 생성 및 검증"
      (let [email "test@example.com"
            purpose :registration
            token (email-token-gateway/generate-token gateway email purpose)
            result (email-token-gateway/verify-token gateway token)]
        (is (string? token))
        (is (f/ok? result))
        (is (= email (:email result)))
        (is (= purpose (:purpose result)))))
    
    (testing "잘못된 토큰 검증"
      (let [result (email-token-gateway/verify-token gateway "invalid-token")]
        (is (f/failed? result))
        (is (= :invalid-token (f/message result))))))) 