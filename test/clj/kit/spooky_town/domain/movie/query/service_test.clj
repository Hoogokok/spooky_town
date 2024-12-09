(ns kit.spooky-town.domain.movie.query.service-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.movie.query.service :as sut]
            [kit.spooky-town.domain.movie.test.repository :refer [->TestMovieRepository]]
            [kit.spooky-town.domain.movie-actor.test.repository :refer [->TestMovieActorRepository]]
            [kit.spooky-town.domain.movie-director.test.repository :refer [->TestMovieDirectorRepository]]
            [kit.spooky-town.infrastructure.persistence.transaction :as tx]))

(def ^:private movie-fixture
  {:movie-id "MOVIE123"
   :uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
   :title "스크림"
   :poster {:url "http://example.com/poster.jpg"}
   :release-date "2024-01-01"
   :genres #{:horror :thriller}
   :release-status :released
   :created-at (java.util.Date.)
   :updated-at (java.util.Date.)})

(def ^:private actors-fixture
  [{:actor-id "ACTOR1"
    :name "네브 캠벨"
    :role "시드니 프레스콧"}
   {:actor-id "ACTOR2"
    :name "데이비드 아퀘트"
    :role "듀이 라일리"}])

(def ^:private directors-fixture
  [{:director-id "DIR1"
    :name "웨스 크레이븐"
    :role "감독"}])

(defn- with-test-movie [f]
  (with-redefs [kit.spooky-town.domain.movie.test.repository/find-by-id
                (constantly movie-fixture)

                kit.spooky-town.domain.movie.test.repository/find-by-criteria
                (constantly [movie-fixture])

                kit.spooky-town.domain.movie.test.repository/count-by-criteria
                (constantly 1)

                kit.spooky-town.domain.movie-actor.test.repository/find-actors-by-movie
                (constantly actors-fixture)

                kit.spooky-town.domain.movie-director.test.repository/find-directors-by-movie
                (constantly directors-fixture)]
    (f)))

(use-fixtures :each with-test-movie)

(deftest movie-query-service-test
  (let [with-read-only (fn [repositories f]
                         (apply f repositories))
        
        service (sut/->MovieQueryServiceImpl
                (->TestMovieRepository)
                (->TestMovieActorRepository)
                (->TestMovieDirectorRepository)
                with-read-only)]

    (testing "영화 상세 정보 조회"
      (testing "모든 관계 정보 포함"
        (let [result (sut/find-movie service {:movie-id "MOVIE123"
                                             :include-actors true
                                             :include-directors true})]
          (is (= "MOVIE123" (:movie-id result)))
          (is (= "스크림" (:title result)))
          (is (= actors-fixture (:actors result)))
          (is (= directors-fixture (:directors result)))))

      (testing "관계 정보 미포함"
        (let [result (sut/find-movie service {:movie-id "MOVIE123"})]
          (is (= "MOVIE123" (:movie-id result)))
          (is (nil? (:actors result)))
          (is (nil? (:directors result))))))

    (testing "영화 목록 검색"
      (let [result (sut/search-movies service {:page 1
                                              :sort-by :title
                                              :sort-order :asc})]
        (is (= 1 (:total-count result)))
        (is (= 1 (:total-pages result)))
        (is (= 1 (count (:movies result))))
        (is (= "스크림" (-> result :movies first :title)))))

    (testing "영화 요약 정보 조회"
      (let [result (sut/get-movie-summary service {:movie-id "MOVIE123"})]
        (is (= "MOVIE123" (:movie-id result)))
        (is (= "스크림" (:title result)))
        (is (= "http://example.com/poster.jpg" (:poster-url result))))))) 