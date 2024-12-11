(ns kit.spooky-town.domain.watch-provider.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.watch-provider.entity :as entity]
            [kit.spooky-town.domain.watch-provider.value :as value]))

(deftest create-watch-provider-test
  (testing "유효한 데이터로 WatchProvider 생성"
    (let [now (java.util.Date.)
          test-uuid (java.util.UUID/randomUUID)
          watch-provider (entity/create-watch-provider
                          {:provider-id "NETFLIX"
                           :provider-name "Netflix"
                           :logo-url "https://example.com/netflix.png"
                           :created-at now
                           :uuid test-uuid})]
      (is (some? watch-provider))
      (is (= "NETFLIX" (:provider-id watch-provider)))
      (is (= "Netflix" (:provider-name watch-provider)))
      (is (= "https://example.com/netflix.png" (:logo-url watch-provider)))
      (is (= now (:created-at watch-provider)))
      (is (uuid? (:uuid watch-provider)))))

  (testing "logo-url이 없어도 WatchProvider 생성 가능"
    (let [now (java.util.Date.)
          test-uuid (java.util.UUID/randomUUID)
          watch-provider (entity/create-watch-provider
                          {:provider-id "WAVVE"
                           :provider-name "Wavve"
                           :created-at now
                           :uuid test-uuid})]
      (is (some? watch-provider))
      (is (nil? (:logo-url watch-provider))))))

  (testing "필수 필드가 없으면 WatchProvider 생성 실패"
    (is (nil? (entity/create-watch-provider
                {:provider-id "NETFLIX"})))
    (is (nil? (entity/create-watch-provider
                {:provider-name "Netflix"})))
    (is (nil? (entity/create-watch-provider
                {:provider-id "NETFLIX"
                 :provider-name "Netflix"}))))