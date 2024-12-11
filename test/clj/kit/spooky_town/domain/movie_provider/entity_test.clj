(ns kit.spooky-town.domain.movie-provider.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.movie-provider.entity :as entity]))

(deftest create-movie-provider-test
  (testing "유효한 데이터로 MovieProvider 생성"
    (let [now (java.util.Date.)
          test-uuid (random-uuid)
          movie-provider (entity/create-movie-provider
                          {:movie-provider-id "MP123"
                           :movie-id "MV456"
                           :provider-id "provider!@#!"
                           :created-at now
                           :uuid test-uuid})]
      (is (some? movie-provider))
      (is (= "MP123" (:movie-provider-id movie-provider)))
      (is (= "MV456" (:movie-id movie-provider)))
      (is (= "provider!@#!" (:provider-id movie-provider)))
      (is (= now (:created-at movie-provider)))
      (is (= test-uuid (:uuid movie-provider)))))

  (testing "필수 필드가 없으면 MovieProvider 생성 실패"
    (is (nil? (entity/create-movie-provider
                {:movie-id "MV456"
                 :provider-id "provider!@#!"})))
    
    (is (nil? (entity/create-movie-provider
                {:movie-provider-id "MP123"
                 :provider-id "provider!@#!"})))
    
    (is (nil? (entity/create-movie-provider
                {:movie-provider-id "MP123"
                 :movie-id "MV456"})))
    
    (is (nil? (entity/create-movie-provider
                {:movie-provider-id "MP123"
                 :movie-id "MV456"
                 :provider-id "provider!@#!"}))))) 