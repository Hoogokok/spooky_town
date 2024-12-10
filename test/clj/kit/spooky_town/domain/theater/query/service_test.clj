(ns kit.spooky-town.domain.theater.query.service-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.theater.query.service :as sut]
            [kit.spooky-town.domain.theater.test.repository :refer [->TestTheaterRepository]]
            [kit.spooky-town.domain.theater.repository.protocol :as repo]))

(def ^:private theater-fixture
  {:theater-id "01HQ1234567890ABCDEFGHJKLM"
   :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
   :chain-type :cgv
   :created-at (java.util.Date.)
   :updated-at (java.util.Date.)})

(defn- with-test-theater [f]
  (with-redefs [repo/find-by-uuid
                (fn [_ uuid]
                  (when (= uuid (:uuid theater-fixture))
                    theater-fixture))

                repo/find-by-chain-type
                (fn [_ chain-type]
                  (when (= chain-type :cgv)
                    [theater-fixture]))]
    (f)))

(use-fixtures :each with-test-theater)

(deftest theater-query-service-test
  (let [with-read-only (fn [repository f]
                         (f repository))

        service (sut/->TheaterQueryServiceImpl
                 (->TestTheaterRepository)
                 with-read-only)]

    (testing "극장 상세 정보 조회"
      (testing "존재하는 극장"
        (let [result (sut/find-theater service {:theater-uuid (:uuid theater-fixture)})]
          (is (some? result))
          (is (= (:uuid theater-fixture) (:uuid result)))
          (is (= (:chain-type theater-fixture) (:chain-type result)))))

      (testing "존재하지 않는 극장"
        (is (nil? (sut/find-theater
                   service
                   {:theater-uuid #uuid "550e8400-e29b-41d4-a716-446655440001"})))))

    (testing "체인 타입으로 극장 검색"
      (testing "존재하는 체인 타입"
        (let [result (sut/search-theaters service {:chain-type :cgv})]
          (is (= 1 (count result)))
          (is (= (:uuid theater-fixture) (-> result first :uuid)))
          (is (= :cgv (-> result first :chain-type)))))

      (testing "존재하지 않는 체인 타입"
        (let [result (sut/search-theaters service {:chain-type :unknown})]
          (is (empty? result)))))

    (testing "극장 요약 정보 조회"
      (testing "존재하는 극장"
        (let [result (sut/get-theater-summary service {:theater-uuid (:uuid theater-fixture)})]
          (is (some? result))
          (is (= (:uuid theater-fixture) (:uuid result)))
          (is (= "CGV" (:chain-name result)))))

      (testing "존재하지 않는 극장"
        (is (nil? (sut/get-theater-summary
                   service
                   {:theater-uuid #uuid "550e8400-e29b-41d4-a716-446655440001"})))))))