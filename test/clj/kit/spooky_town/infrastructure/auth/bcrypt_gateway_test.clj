(ns kit.spooky-town.infrastructure.auth.bcrypt-gateway-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.infrastructure.auth.bcrypt-gateway :refer [->BCryptPasswordGateway]]
            [kit.spooky-town.domain.user.gateway.password :refer [hash-password verify-password]]))

(deftest bcrypt-password-gateway-test
  (let [gateway (->BCryptPasswordGateway)]
    
    (testing "비밀번호 해시화"
      (let [password "test123!"
            hashed (hash-password gateway password)]
        (is (string? hashed))
        (is (not= password hashed))
        ;; salt가 적용되어 매번 다른 해시값이 생성되는지 확인
        (is (not= hashed (hash-password gateway password)))))
    
    (testing "올바른 비밀번호 검증"
      (let [password "test123!"
            hashed (hash-password gateway password)]
        (is (verify-password gateway password hashed))))
    
    (testing "잘못된 비밀번호 검증"
      (let [password "test123!"
            wrong-password "wrong123!"
            hashed (hash-password gateway password)]
        (is (not (verify-password gateway wrong-password hashed))))))) 