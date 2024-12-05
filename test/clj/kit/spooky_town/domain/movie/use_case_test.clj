(ns kit.spooky-town.domain.movie.use-case-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [kit.spooky-town.domain.movie.use-case :as use-case :refer [->CreateMovieUseCaseImpl]]
            [kit.spooky-town.domain.movie.test.repository :as movie-repository-fixture :refer [->TestMovieRepository]]
            [kit.spooky-town.domain.director.test.repository :as director-repository-fixture :refer [->TestDirectorRepository]]
            [kit.spooky-town.domain.actor.test.repository :as actor-repository-fixture :refer [->TestActorRepository]]
            [kit.spooky-town.domain.common.image.test.gateway :as image-gateway-fixture :refer [->TestImageUploadGateway]]
            [kit.spooky-town.domain.common.id.test.generator :as id-generator-fixture :refer [->TestIdGenerator]]))

(deftest create-movie-test
  (let [with-tx (fn [repo f] (f repo))
        movie-repository (->TestMovieRepository)
        director-repository (->TestDirectorRepository)
        actor-repository (->TestActorRepository)
        image-gateway (->TestImageUploadGateway)
        id-generator (->TestIdGenerator)
        movie-use-case (->CreateMovieUseCaseImpl with-tx 
                                                movie-repository 
                                                director-repository 
                                                actor-repository 
                                                image-gateway 
                                                id-generator)]

    (testing "영화 생성 성공 - 새로운 감독"
      (with-redefs [id-generator-fixture/generate-ulid 
                   (constantly "01HGD3V7XN6QX5RJQR1VVXDCP9")
                   director-repository-fixture/find-by-name 
                   (constantly nil)  ;; 감독이 없는 상황
                   director-repository-fixture/save! 
                   (fn [_ director] director)  ;; 감독 저장
                   movie-repository-fixture/save! 
                   (fn [_ movie] movie)]  ;; 영화 저장
        (let [command {:title "스푸키 타운의 비밀"
                      :director-names ["봉준호"]
                      :release-info {:release-status :upcoming
                                   :release-date "2024-12-25"}
                      :genres #{:horror :psychological}}
              result (use-case/create-movie movie-use-case command)]
          (is (f/ok? result))
          (is (= "01HGD3V7XN6QX5RJQR1VVXDCP9" (:movie-id result)))
          (is (= "스푸키 타운의 비밀" (:title result)))
          (is (= ["01HGD3V7XN6QX5RJQR1VVXDCP9"] (:director-ids result))))))

    (testing "영화 생성 성공 - 기존 감독"
      (with-redefs [id-generator-fixture/generate-ulid 
                   (constantly "01HGD3V7XN6QX5RJQR1VVXDCP9")
                   director-repository-fixture/find-by-name 
                   (fn [_ _] {:director-id "existing-director-id"})  ;; 기존 감독 찾기
                   movie-repository-fixture/save! 
                   (fn [_ movie] movie)]
        (let [command {:title "스푸키 타운의 비밀"
                      :director-names ["봉준호"]
                      :release-info {:release-status :upcoming
                                   :release-date "2024-12-25"}
                      :genres #{:horror :psychological}}
              result (use-case/create-movie movie-use-case command)]
          (is (f/ok? result))
          (is (= ["existing-director-id"] (:director-ids result))))))))