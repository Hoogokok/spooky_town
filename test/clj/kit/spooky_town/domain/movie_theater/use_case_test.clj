(ns kit.spooky-town.domain.movie-theater.use-case-test
  (:require [clojure.test :refer :all]
            [kit.spooky-town.domain.movie-theater.use-case :as use-case :refer [->MovieTheaterUseCaseImpl]]
            [kit.spooky-town.domain.movie-theater.test.repository :as movie-theater-repository-fixture :refer [->TestMovieTheaterRepository]]))

(def movie-id "01HQ1234567890ABCDEFGHJKLM")
(def theater-id "01HQ1234567890ABCDEFGHJKLN")

(deftest movie-theater-use-case-test
  (let [with-tx (fn [[repo] f] (f repo))
        movie-theater-repository (->TestMovieTheaterRepository)
        use-case (->MovieTheaterUseCaseImpl movie-theater-repository with-tx)]

    (testing "영화-극장 할당"
      (with-redefs [movie-theater-repository-fixture/save-movie-theater!
                    (fn [_ m-id t-id]
                      {:movie-id m-id
                       :theater-id t-id})]
        (let [command {:movie-id movie-id
                      :theater-id theater-id}
              result (use-case/assign-theater! use-case command)]
          (is (some? result))
          (is (= movie-id (:movie-id result)))
          (is (= theater-id (:theater-id result))))))

    (testing "영화-극장 관계 제거"
      (with-redefs [movie-theater-repository-fixture/delete-movie-theater!
                    (fn [_ m-id t-id]
                      (and (= m-id movie-id)
                           (= t-id theater-id)))]
        (let [command {:movie-id movie-id
                      :theater-id theater-id}]
          (is (true? (use-case/remove-theater! use-case command))))))

    (testing "영화의 극장 목록 조회"
      (let [theaters [{:theater-id theater-id :name "CGV 강남"}]]
        (with-redefs [movie-theater-repository-fixture/find-theaters-by-movie
                      (fn [_ m-id]
                        (when (= m-id movie-id)
                          theaters))]
          (let [command {:movie-id movie-id}
                result (use-case/get-theaters use-case command)]
            (is (= theaters result))))))

    (testing "극장의 영화 목록 조회"
      (let [movies [{:movie-id movie-id :title "스푸키 타운의 비밀"}]]
        (with-redefs [movie-theater-repository-fixture/find-movies-by-theater
                      (fn [_ t-id]
                        (when (= t-id theater-id)
                          movies))]
          (let [command {:theater-id theater-id}
                result (use-case/get-movies use-case command)]
            (is (= movies result)))))))) 