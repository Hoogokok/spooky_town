(ns kit.spooky-town.infrastructure.email.smtp-gateway-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.infrastructure.email.smtp-gateway :as smtp]
            [kit.spooky-town.domain.user.gateway.email :as email-gateway]
            [postal.core :as postal]))

(deftest smtp-email-gateway-test
  (let [sent-emails (atom [])
        test-smtp-config {:host "test-smtp.example.com"
                         :port 587
                         :user "test@example.com"
                         :pass "test-password"
                         :from "noreply@example.com"}
        test-base-url "http://test.example.com"
        gateway (smtp/->SMTPEmailGateway test-smtp-config test-base-url)]
    
    (testing "회원가입 인증 이메일 발송"
      (with-redefs [postal/send-message (fn [_ msg] (swap! sent-emails conj msg) {:code 0})]
        (email-gateway/send-verification-email gateway "user@example.com" "test-token")
        (let [sent-email (last @sent-emails)]
          (is (= "user@example.com" (:to sent-email)))
          (is (= "스푸키 타운 회원가입 인증" (:subject sent-email)))
          (is (re-find #"test-token" (get-in sent-email [:body 1 :html])))))))) 