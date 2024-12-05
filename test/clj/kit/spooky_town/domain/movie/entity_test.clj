(ns kit.spooky-town.domain.movie.entity-test) (ns kit.spooky-town.domain.movie.entity-test
                                                 (:require [clojure.test :refer :all]
                                                           [kit.spooky-town.domain.movie.entity :as entity]))

(def valid-movie-data
  {:movie-id "01HGD3V7XN6QX5RJQR1VVXDCP9"  ;; ULID
   :uuid #uuid "00000000-0000-0000-0000-000000000000"
   :title "스푸키 타운의 비밀"
   :director-ids ["01HGD3V7XN6QX5RJQR1VVXDCP8"  ;; ULID
                  "01HGD3V7XN6QX5RJQR1VVXDCP7"]
   :release-info {:release-status :upcoming
                  :release-date "2024-12-25"}
   :genres #{:horror :psychological}
   :created-at (java.util.Date.)
   :updated-at (java.util.Date.)})

(def valid-movie-with-optional
  (assoc valid-movie-data
         :movie-actors [{:actor-id "01HGD3V7XN6QX5RJQR1VVXDCP6"  ;; ULID
                         :role "주연"}]
         :runtime 120
         :poster {:url "http://example.com/poster.jpg"
                  :width 600
                  :height 800}))


(deftest create-movie-test
  (testing "필수 속성으로 영화 생성"
    (let [movie (entity/create-movie valid-movie-data)]
      (testing "기본 속성 검증"
        (is (some? movie))
        (is (= (:movie-id movie) (:movie-id valid-movie-data)))
        (is (= (:uuid movie) (:uuid valid-movie-data)))
        (is (= (:title movie) (:title valid-movie-data)))
        (is (= (:director-ids movie) (:director-ids valid-movie-data)))
        (is (= (:release-info movie) (:release-info valid-movie-data)))
        (is (= (:genres movie) (:genres valid-movie-data)))
        (is (inst? (:created-at movie)))
        (is (inst? (:updated-at movie))))))

  (testing "선택 속성을 포함한 영화 생성" 
    (println "valid-movie-with-optional:" valid-movie-with-optional)
    (let [movie (entity/create-movie valid-movie-with-optional)]
      (println "Movie-with-optional:" movie)
     ))

  (testing "영화 생성 실패 케이스"
    (testing "movie-id 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :movie-id)))))

    (testing "UUID 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :uuid)))))

    (testing "제목 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :title)))))

    (testing "감독 ID 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :director-ids)))))

    (testing "감독 ID가 빈 배열인 경우"
      (is (nil? (entity/create-movie (assoc valid-movie-data :director-ids [])))))

    (testing "잘못된 타입의 필드"
      (testing "movie-id가 문자열이 아닌 경우"
        (is (nil? (entity/create-movie (assoc valid-movie-data :movie-id 123)))))

      (testing "director-ids가 문자열 배열이 아닌 경우"
        (is (nil? (entity/create-movie (assoc valid-movie-data :director-ids [1 2])))))

      (testing "runtime이 양의 정수가 아닌 경우"
        (is (nil? (entity/create-movie
                   (assoc valid-movie-with-optional :runtime -10))))))))