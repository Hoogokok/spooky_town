(ns kit.spooky-town.domain.theater.use-case-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.theater.use-case :as use-case :refer [->TheaterUseCaseImpl]]
            [kit.spooky-town.domain.theater.test.repository :as theater-repository-fixture :refer [->TestTheaterRepository]]
            [kit.spooky-town.domain.common.id.test.generator :as id-generator-fixture :refer [->TestIdGenerator]]
            [kit.spooky-town.domain.common.id.test.uuid-generator :as uuid-generator-fixture :refer [->TestUuidGenerator]]))

(def base-command
  {:chain-type :cgv})

(deftest theater-use-case-test
  (let [with-tx (fn [[repo] f] (f repo))
        theater-repository (->TestTheaterRepository)
        id-generator (->TestIdGenerator)
        uuid-generator (->TestUuidGenerator)
        use-case (->TheaterUseCaseImpl 
                  theater-repository 
                  id-generator 
                  uuid-generator 
                  with-tx)]

    (testing "극장 생성"
      (with-redefs [id-generator-fixture/generate-ulid (constantly "01HQ1234567890ABCDEFGHJKLM")
                    theater-repository-fixture/save! (fn [_ theater] theater)]
        (let [result (use-case/create-theater! use-case base-command)]
          (is (some? result))
          (is (= (:chain-type base-command) (:chain-type result)))
          (is (= "01HQ1234567890ABCDEFGHJKLM" (:theater-id result)))
          (is (= #uuid "550e8400-e29b-41d4-a716-446655440000" (:uuid result))))))

    (testing "극장 정보 수정"
      (let [theater-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
            existing-theater {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                            :uuid theater-uuid
                            :chain-type :cgv
                            :created-at (java.util.Date.)
                            :updated-at (java.util.Date.)}]
        
        (testing "존재하는 극장"
          (with-redefs [theater-repository-fixture/find-by-uuid
                       (fn [_ uuid] (when (= uuid theater-uuid) existing-theater))
                       theater-repository-fixture/save! (fn [_ theater] theater)]
            (let [command {:theater-uuid theater-uuid
                          :chain-type :megabox}
                  result (use-case/update-theater! use-case command)]
              (is (some? result))
              (is (= :megabox (:chain-type result)))
              (is (= (:theater-id existing-theater) (:theater-id result))))))

        (testing "존재하지 않는 극장"
          (with-redefs [theater-repository-fixture/find-by-uuid (constantly nil)]
            (let [command {:theater-uuid #uuid "550e8400-e29b-41d4-a716-446655440001"
                          :chain-type :megabox}]
              (is (nil? (use-case/update-theater! use-case command))))))))

    (testing "극장 삭제"
      (let [theater-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
            existing-theater {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                            :uuid theater-uuid
                            :chain-type :cgv
                            :created-at (java.util.Date.)
                            :updated-at (java.util.Date.)}]
        
        (testing "존재하는 극장"
          (with-redefs [theater-repository-fixture/find-by-uuid
                       (fn [_ uuid] (when (= uuid theater-uuid) existing-theater))
                       theater-repository-fixture/delete! (fn [_ _] true)]
            (let [command {:theater-uuid theater-uuid}]
              (is (true? (use-case/delete-theater! use-case command))))))

        (testing "존재하지 않는 극장"
          (with-redefs [theater-repository-fixture/find-by-uuid (constantly nil)]
            (let [command {:theater-uuid #uuid "550e8400-e29b-41d4-a716-446655440001"}]
              (is (nil? (use-case/delete-theater! use-case command))))))))))

