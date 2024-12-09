(ns kit.spooky-town.domain.movie.entity-test) (ns kit.spooky-town.domain.movie.entity-test
                                                 (:require [clojure.test :refer :all]
                                                           [kit.spooky-town.domain.movie.entity :as entity]))

(def valid-movie-data
  {:movie-id "01HGD3V7XN6QX5RJQR1VVXDCP9"  ;; ULID
   :uuid #uuid "00000000-0000-0000-0000-000000000000"
   :title "스푸키 타운의 비밀"
   :release-info {:release-status :upcoming
                  :release-date "2024-12-25"}
   :genres #{:horror :psychological}
   :created-at (java.util.Date.)
   :updated-at (java.util.Date.)})

(def valid-movie-with-optional
  (assoc valid-movie-data
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
        (is (= (:release-info movie) (:release-info valid-movie-data)))
        (is (= (:genres movie) (:genres valid-movie-data)))
        (is (inst? (:created-at movie)))
        (is (inst? (:updated-at movie))))))

  (testing "선택 속성을 포함한 영화 생성"
    (let [movie (entity/create-movie valid-movie-with-optional)]
      (is (some? movie))
  (is (= (:runtime movie) (:runtime valid-movie-with-optional)))
  (is (= (:poster movie) (:poster valid-movie-with-optional)))))

  (testing "영화 생성 실패 케이스"
    (testing "movie-id 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :movie-id)))))

    (testing "UUID 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :uuid)))))

    (testing "제목 없는 경우"
      (is (nil? (entity/create-movie (dissoc valid-movie-data :title)))))

    (testing "잘못된 타입의 필드"
      (testing "movie-id가 문자열이 아닌 경우"
        (is (nil? (entity/create-movie (assoc valid-movie-data :movie-id 123)))))

      (testing "runtime이 양의 정수가 아닌 경우"
        (is (nil? (entity/create-movie
                   (assoc valid-movie-with-optional :runtime -10))))))))

(deftest movie-summary-test
  (testing "영화 엔티티를 요약 정보로 변환"
    (let [movie {:movie-id "MOVIE123"
                 :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
                 :title "스크림"
                 :poster {:url "http://example.com/poster.jpg"}
                 :release-date "2024-01-01"
                 :genres #{:horror :thriller}
                 :directors [{:name "웨스 크레이븐"}]
                 :release-status :released
                 :created-at (java.util.Date.)
                 :updated-at (java.util.Date.)}
          summary (entity/->summary movie)]
      
      (testing "필수 필드 포함 여부"
        (is (= "MOVIE123" (:movie-id summary)))
        (is (= "스크림" (:title summary)))
        (is (= "http://example.com/poster.jpg" (:poster-url summary)))
        (is (= "2024-01-01" (:release-date summary)))
        (is (= #{:horror :thriller} (:genres summary)))
        (is (= ["웨스 크레이븐"] (:director-names summary)))
        (is (= :released (:release-status summary))))
      
      (testing "불필요한 필드 제외 여부"
        (is (nil? (:uuid summary)))
        (is (nil? (:created-at summary)))
        (is (nil? (:updated-at summary))))))

  (testing "유효하지 않은 영화 엔티티 처리"
    (is (nil? (entity/->summary {})))
    (is (nil? (entity/->summary nil)))))

(deftest update-movie-test
  (let [movie (entity/create-movie valid-movie-data)]
    
    (testing "전체 필드 업데이트"
      (let [updates {:title "새로운 제목"
                    :runtime 120
                    :genres #{:horror :gore}
                    :release-info {:release-status :released
                                 :release-date "2024-01-01"}}
            updated (entity/update-movie movie updates)]
        (is (some? updated))
        (is (= "새로운 제목" (:title updated)))
        (is (= 120 (:runtime updated)))
        (is (= #{:horror :gore} (:genres updated)))
        (is (= (:release-info updates) (:release-info updated)))
        (is (not= (:updated-at movie) (:updated-at updated)))))

    (testing "선택적 필드 업데이트"
      (testing "제목만 업데이트"
        (let [updates {:title "새로운 제목"}
              updated (entity/update-movie movie updates)]
          (is (some? updated))
          (is (= "새로운 제목" (:title updated)))
          (is (= (:runtime movie) (:runtime updated)))
          (is (= (:genres movie) (:genres updated)))
          (is (= (:release-info movie) (:release-info updated)))
          (is (not= (:updated-at movie) (:updated-at updated)))))

      (testing "상영시간만 업데이트"
        (let [updates {:runtime 120}
              updated (entity/update-movie movie updates)]
          (is (some? updated))
          (is (= (:title movie) (:title updated)))
          (is (= 120 (:runtime updated)))
          (is (= (:genres movie) (:genres updated)))
          (is (= (:release-info movie) (:release-info updated)))
          (is (not= (:updated-at movie) (:updated-at updated)))))

      (testing "장르만 업데이트"
        (let [updates {:genres #{:horror :gore}}
              updated (entity/update-movie movie updates)]
          (is (some? updated))
          (is (= (:title movie) (:title updated)))
          (is (= (:runtime movie) (:runtime updated)))
          (is (= #{:horror :gore} (:genres updated)))
          (is (= (:release-info movie) (:release-info updated)))
          (is (not= (:updated-at movie) (:updated-at updated)))))

      (testing "개봉정보만 업데이트"
        (let [updates {:release-info {:release-status :released
                                    :release-date "2024-01-01"}}
              updated (entity/update-movie movie updates)]
          (is (some? updated))
          (is (= (:title movie) (:title updated)))
          (is (= (:runtime movie) (:runtime updated)))
          (is (= (:genres movie) (:genres updated)))
          (is (= (:release-info updates) (:release-info updated)))
          (is (not= (:updated-at movie) (:updated-at updated))))))

    (testing "유효하지 않은 업데이트"
      (testing "빈 제목"
        (is (nil? (entity/update-movie movie {:title ""}))))
      
      (testing "잘못된 상영시간"
        (is (nil? (entity/update-movie movie {:runtime -10}))))
      
      (testing "필수 장르 누락"
        (is (nil? (entity/update-movie movie {:genres #{:gore :psychological}}))))
      
      (testing "잘못된 개봉일"
        (is (nil? (entity/update-movie movie 
                   {:release-info {:release-status :released
                                 :release-date "2024-13-45"}}))))) 
    
    (testing "포스터 업데이트"
      (let [new-poster {:url "http://example.com/new-poster.jpg"
                       :width 800
                       :height 1200}
            updates {:poster new-poster}
            updated (entity/update-movie movie updates)]
        (is (some? updated))
        (is (= new-poster (:poster updated)))
        (is (not= (:updated-at movie) (:updated-at updated)))))

    (testing "포스터를 포함한 복합 업데이트"
      (let [updates {:title "새로운 제목"
                    :poster {:url "http://example.com/new-poster.jpg"
                            :width 800
                            :height 1200}}
            updated (entity/update-movie movie updates)]
        (is (some? updated))
        (is (= "새로운 제목" (:title updated)))
        (is (= (:url (:poster updates)) (:url (:poster updated))))
        (is (not= (:updated-at movie) (:updated-at updated)))))

    (testing "유효하지 않은 포스터 업데이트"
      (testing "잘못된 포스터 데이터"
        (is (nil? (entity/update-movie movie 
                   {:poster {:url ""}})))))))