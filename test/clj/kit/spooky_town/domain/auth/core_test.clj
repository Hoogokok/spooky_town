(ns kit.spooky-town.domain.auth.core-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.auth.core :as auth :refer [->Credentials]]))

(deftest credentials-test
  (testing "Credentials 생성"
    (let [credentials (->Credentials "test@example.com" "password123")]
      (is (= "test@example.com" (:email credentials)))
      (is (= "password123" (:password credentials)))))) 