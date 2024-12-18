(ns kit.spooky-town.domain.theater.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.theater.entity :as entity]
            [kit.spooky-town.domain.theater.value :as value]))

(deftest create-theater-test
  (testing "유효한 데이터로 극장 생성"
    (let [theater-data {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                       :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :chain-type :cgv}
          theater (entity/create-theater theater-data)]
      (testing "극장이 성공적으로 생성됨"
        (is (some? theater))
        (is (= (:theater-id theater-data) (:theater-id theater)))
        (is (= (:uuid theater-data) (:uuid theater)))
        (is (= (:chain-type theater-data) (:chain-type theater)))
        (is (instance? java.util.Date (:created-at theater)))
        (is (instance? java.util.Date (:updated-at theater))))))

  (testing "잘못된 체인 타입으로 극장 생성 시도"
    (let [theater-data {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                       :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :chain-type :invalid-chain}
          theater (entity/create-theater theater-data)]
      (is (nil? theater))))

  (testing "필수 필드 누락 시 극장 생성 실패"
    (let [invalid-data {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                       :chain-type :cgv}
          theater (entity/create-theater invalid-data)]
      (is (nil? theater)))))

(deftest theater-summary-test
  (testing "극장 요약 정보 변환"
    (let [theater (entity/create-theater
                   {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                    :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                    :chain-type :cgv})
          summary (entity/->summary theater)]
      (testing "요약 정보가 올바르게 생성됨"
        (is (= (:uuid theater) (:uuid summary)))
        (is (= "CGV" (:chain-name summary)))))))

(deftest theater-validation-test
  (testing "유효한 극장 검증"
    (let [theater (entity/create-theater
                   {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                    :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                    :chain-type :cgv})]
      (is (entity/valid? theater))))

  (testing "각 체인 타입별 극장 생성 검증"
    (doseq [chain-type [:cgv :megabox :lotte]]
      (testing (str chain-type " 체인 타입으로 극장 생성")
        (let [theater (entity/create-theater
                       {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                        :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                        :chain-type chain-type})]
          (is (some? theater))
          (is (= chain-type (:chain-type theater)))
          (is (entity/valid? theater)))))))

(deftest update-theater-test
  (testing "극장 정보 업데이트"
    (let [theater (entity/create-theater
                   {:theater-id "01HQ1234567890ABCDEFGHJKLM"
                    :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                    :chain-type :cgv})]

      (testing "체인 타입 변경"
        (let [updated (entity/update-theater theater {:chain-type :megabox})]
          (testing "성공적으로 업데이트됨"
            (is (some? updated))
            (is (= :megabox (:chain-type updated)))
            (is (= (:theater-id theater) (:theater-id updated)))
            (is (= (:uuid theater) (:uuid updated)))
            (is (= (:created-at theater) (:created-at updated)))
            (is (instance? java.util.Date (:updated-at updated))))))

      (testing "잘못된 체인 타입으로 업데이트 시도"
        (let [updated (entity/update-theater theater {:chain-type :invalid-chain})]
          (is (nil? updated))))

      (testing "업데이트 시간이 갱신됨"
        (let [before-update (:updated-at theater)
              _ (Thread/sleep 1)  ; 시간 차이를 보장하기 위해
              updated (entity/update-theater theater {:chain-type :megabox})]
          (is (> (.getTime (:updated-at updated))
                 (.getTime before-update)))))))) 