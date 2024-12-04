(ns kit.spooky-town.domain.movie.entity-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.movie.entity :as entity]))

(def valid-movie-data
  {:uuid #uuid "00000000-0000-0000-0000-000000000000"
   :title "스푸키 타운의 비밀"
   :director-ids [1 2]
   :release-info {:release-status :upcoming
                 :release-date "2024-12-25"}
   :genres #{:horror :psychological}})

(deftest create-movie-test
  (testing "필수 속성으로 영화 생성"
    (let [movie (entity/create-movie valid-movie-data)]
      (testing "기본 속성 검증"
        (is (some? movie))
        (is (= (:uuid movie) (:uuid valid-movie-data)))
        (is (= (:title movie) "스푸키 타운의 비밀"))
        (is (= (:director-ids movie) [1 2]))
        (is (= (get-in movie [:release-info :release-status]) :upcoming))
        (is (contains? (:genres movie) :horror)))

      (testing "생성/수정 시간 자동 설정"
        (is (inst? (:created-at movie)))
        (is (inst? (:updated-at movie)))
        (is (= (:created-at movie) (:updated-at movie))))))

  (testing "영화 생성 실패 케이스"
    (testing "UUID 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :uuid)))))

    (testing "제목 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :title)))))
    
    (testing "감독 ID 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :director-ids)))))
    
    (testing "감독 ID가 빈 배열인 경우"
      (is (nil? (entity/create-movie (assoc valid-movie-data :director-ids [])))))
    
    (testing "장르에 horror/thriller 둘 다 없는 경우"
      (is (nil? (entity/create-movie 
                 (assoc valid-movie-data :genres #{:psychological :zombie}))))))) 