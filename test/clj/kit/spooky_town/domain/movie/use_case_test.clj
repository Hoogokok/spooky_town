(ns kit.spooky-town.domain.movie.use-case-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [kit.spooky-town.domain.movie.use-case :as use-case :refer [->CreateMovieUseCaseImpl]]
            [kit.spooky-town.domain.movie.test.repository :as movie-repository-fixture :refer [->TestMovieRepository]]
            [kit.spooky-town.domain.director.test.repository :as director-repository-fixture :refer [->TestDirectorRepository]]
            [kit.spooky-town.domain.actor.test.repository :as actor-repository-fixture :refer [->TestActorRepository]]
            [kit.spooky-town.domain.common.id.test.generator :as id-generator-fixture :refer [->TestIdGenerator]]
            [kit.spooky-town.domain.common.id.test.uuid-generator :as uuid-generator-fixture :refer [->TestUuidGenerator]]
            [kit.spooky-town.domain.movie-director.test.repository :as movie-director-repository-fixture :refer [->TestMovieDirectorRepository]]
            [kit.spooky-town.domain.movie-actor.test.repository :as movie-actor-repository-fixture :refer [->TestMovieActorRepository]]
            [kit.spooky-town.domain.common.image.test.gateway :as image-gateway-fixture :refer [->TestImageUploadGateway]]))

(def base-command
  {:title "스푸키 타운의 비밀"
   :director-infos [{:director-name "봉준호" :role "메인 감독"}]
   :release-info {:release-status :upcoming
                  :release-date "2024-12-25"}
   :genres #{:horror :psychological}})

(deftest create-movie-test
  (let [with-tx (fn [repo f] (f repo))
        movie-repository (->TestMovieRepository)
        director-repository (->TestDirectorRepository)
        movie-director-repository (->TestMovieDirectorRepository)
        actor-repository (->TestActorRepository)
        movie-actor-repository (->TestMovieActorRepository)
        image-gateway (->TestImageUploadGateway)
        id-generator (->TestIdGenerator)
        uuid-generator (->TestUuidGenerator)
        movie-use-case (->CreateMovieUseCaseImpl with-tx
                                                 movie-repository
                                                 movie-director-repository
                                                 movie-actor-repository
                                                 director-repository
                                                 actor-repository
                                                 image-gateway
                                                 id-generator
                                                 uuid-generator)]

    (testing "영화 생성 성공 - 필수 필드만"
      (with-redefs [id-generator-fixture/generate-ulid (constantly "test-id")
                    director-repository-fixture/find-by-name (constantly nil)
                    director-repository-fixture/save! (fn [_ director] director)
                    movie-director-repository-fixture/save-movie-director!
                    (fn [_ movie-id director-id role]
                      {:movie-id movie-id :director-id director-id :role role})
                    movie-repository-fixture/save! (fn [_ movie] movie)]
        (let [result (use-case/create-movie movie-use-case base-command)]
          (is (f/ok? result))
          (is (= "test-id" result)))))
    
    (testing "영화 생성 성공 - 선택적 필드 포함"
      (let [command (assoc base-command
                           :runtime 120
                           :poster-file "path/to/poster.jpg")]
        (with-redefs [id-generator-fixture/generate-ulid (constantly "test-id")
                      director-repository-fixture/find-by-name (constantly nil)
                      director-repository-fixture/save! (fn [_ director] director)
                      movie-director-repository-fixture/save-movie-director!
                      (fn [_ movie-id director-id role]
                        {:movie-id movie-id :director-id director-id :role role})
                      movie-repository-fixture/save! (fn [_ movie] movie)
                      image-gateway-fixture/upload (fn [_ _] {:url "http://example.com/poster.jpg"})]
          (let [result (use-case/create-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-id" result))))))

    (testing "영화 생성 실패 - 필수 필드 누락"
      (testing "제목 누락"
        (let [command (dissoc base-command :title)]
          (is (f/failed? (use-case/create-movie movie-use-case command)))))

      (testing "감독 정보 누락"
        (let [command (dissoc base-command :director-infos)]
          (is (f/failed? (use-case/create-movie movie-use-case command)))))

      (testing "개봉 정보 누락"
        (let [command (dissoc base-command :release-info)]
          (is (f/failed? (use-case/create-movie movie-use-case command)))))

      (testing "장르 누락"
        (let [command (dissoc base-command :genres)]
          (is (f/failed? (use-case/create-movie movie-use-case command))))))))