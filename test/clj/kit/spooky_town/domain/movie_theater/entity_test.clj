(ns kit.spooky-town.domain.movie-theater.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.movie-theater.entity :as entity]
            [kit.spooky-town.domain.movie-theater.value :as value]))

(deftest create-movie-theater-test
  (testing "유효한 데이터로 영화-극장 관계 생성"
    (let [movie-theater-data {:movie-id "01HQ1234567890ABCDEFGHJKLM"
                             :theater-id "01HQ1234567890ABCDEFGHJKLN"}
          movie-theater (entity/create-movie-theater movie-theater-data)]
      (testing "영화-극장 관계가 성공적으로 생성됨"
        (is (some? movie-theater))
        (is (= (:movie-id movie-theater-data) (:movie-id movie-theater)))
        (is (= (:theater-id movie-theater-data) (:theater-id movie-theater)))
        (is (instance? java.util.Date (:created-at movie-theater))))))

  (testing "필수 필드 누락 시 생성 실패"
    (testing "movie-id 누락"
      (let [invalid-data {:theater-id "01HQ1234567890ABCDEFGHJKLN"}
            movie-theater (entity/create-movie-theater invalid-data)]
        (is (nil? movie-theater))))

    (testing "theater-id 누락"
      (let [invalid-data {:movie-id "01HQ1234567890ABCDEFGHJKLM"}
            movie-theater (entity/create-movie-theater invalid-data)]
        (is (nil? movie-theater)))))

  (testing "타임스탬프 자동 생성"
    (let [movie-theater (entity/create-movie-theater
                         {:movie-id "01HQ1234567890ABCDEFGHJKLM"
                          :theater-id "01HQ1234567890ABCDEFGHJKLN"})]
      (is (instance? java.util.Date (:created-at movie-theater))))))

(deftest movie-theater-validation-test
  (testing "유효한 영화-극장 관계 검증"
    (let [movie-theater (entity/create-movie-theater
                         {:movie-id "01HQ1234567890ABCDEFGHJKLM"
                          :theater-id "01HQ1234567890ABCDEFGHJKLN"})]
      (is (entity/valid? movie-theater))))) 