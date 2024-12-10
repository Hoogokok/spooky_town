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
            [kit.spooky-town.domain.theater.test.repository :as theater-repository-fixture :refer [->TestTheaterRepository]]
            [kit.spooky-town.domain.movie-theater.test.repository :as movie-theater-repository-fixture :refer [->TestMovieTheaterRepository]]
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
        theater-repository (->TestTheaterRepository)
        movie-theater-repository (->TestMovieTheaterRepository)
        image-gateway (->TestImageUploadGateway)
        id-generator (->TestIdGenerator)
        uuid-generator (->TestUuidGenerator)
        movie-use-case (->CreateMovieUseCaseImpl with-tx
                                                 movie-repository
                                                 movie-director-repository
                                                 movie-actor-repository
                                                 movie-theater-repository 
                                                 director-repository
                                                 actor-repository
                                                 theater-repository
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
    
    (testing "영화 생성 성공 - 선택 필드 포함"
      (let [command (assoc base-command
                           :runtime 120
                           :poster-file "path/to/poster.jpg")]
        (with-redefs [id-generator-fixture/generate-ulid (constantly "test-id")
                      director-repository-fixture/find-by-name (constantly nil)
                      director-repository-fixture/save! (fn [_ director] director)
                      movie-director-repository-fixture/save-movie-director!
                      (fn [_ movie-id director-id role]
                        {:movie-id movie-id :director-id director-id :role role})
                      theater-repository-fixture/find-id-by-name (constantly nil)
                      theater-repository-fixture/save! (fn [_ theater] theater)
                      movie-theater-repository-fixture/save-movie-theater!
                      (fn [_ movie-id theater-id]
                        {:movie-id movie-id :theater-id theater-id})
                      movie-repository-fixture/save! (fn [_ movie] movie)
                      image-gateway-fixture/upload (fn [_ _] {:url "http://example.com/poster.jpg"})]
          (let [result (use-case/create-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-id" result))))))

    (testing "영화 생성 성공 - 극장 정보 포함"
      (let [command (assoc base-command
                          :theater-infos [{:theater-name "CGV 강남"}
                                        {:theater-name "메가박스 코엑스"}])]
        (with-redefs [id-generator-fixture/generate-ulid (constantly "test-id")
                     director-repository-fixture/find-by-name (constantly nil)
                     director-repository-fixture/save! (fn [_ director] director)
                     movie-director-repository-fixture/save-movie-director!
                     (fn [_ movie-id director-id role]
                       {:movie-id movie-id :director-id director-id :role role})
                     theater-repository-fixture/find-id-by-name (constantly nil)
                     theater-repository-fixture/save! (fn [_ theater] theater)
                     movie-theater-repository-fixture/save-movie-theater!
                     (fn [_ movie-id theater-id]
                       {:movie-id movie-id :theater-id theater-id})
                     movie-repository-fixture/save! (fn [_ movie] movie)]
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

(deftest update-movie-test
  (let [with-tx (fn [repo f] (f repo))
        movie-repository (->TestMovieRepository)
        director-repository (->TestDirectorRepository)
        movie-director-repository (->TestMovieDirectorRepository)
        actor-repository (->TestActorRepository)
        movie-actor-repository (->TestMovieActorRepository)
        theater-repository (->TestTheaterRepository)
        movie-theater-repository (->TestMovieTheaterRepository)
        image-gateway (->TestImageUploadGateway)
        id-generator (->TestIdGenerator)
        uuid-generator (->TestUuidGenerator)
        movie-use-case (->CreateMovieUseCaseImpl with-tx
                                                movie-repository
                                                movie-director-repository
                                                movie-actor-repository
                                                movie-theater-repository
                                                director-repository
                                                actor-repository
                                                theater-repository
                                                image-gateway
                                                id-generator
                                                uuid-generator)
        existing-movie {:movie-id "test-movie-id"
                       :uuid #uuid "00000000-0000-0000-0000-000000000000"
                       :title "원제목"
                       :release-info {:release-status :upcoming
                                      :release-date "2024-12-25"}
                       :genres #{:horror :psychological}
                       :created-at (java.util.Date.)
                       :updated-at (java.util.Date.)}]

    (testing "영화 업데이트 성공"
      (with-redefs [movie-repository-fixture/find-by-uuid (constantly existing-movie)
                    movie-repository-fixture/save! (fn [_ movie] movie)]
        
        (testing "모든 필드 업데이트"
          (let [command {:movie-id "test-movie-id"
                        :title "새로운 제목"
                        :runtime 120
                        :genres #{:horror :gore}
                        :release-info {:release-status :released
                                     :release-date "2024-01-01"}}
                result (use-case/update-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-movie-id" result))))

        (testing "제목만 업데이트"
          (let [command {:movie-id "test-movie-id"
                        :title "새로운 제목"}
                result (use-case/update-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-movie-id" result))))

        (testing "상영시간만 업데이트"
          (let [command {:movie-id "test-movie-id"
                        :runtime 120}
                result (use-case/update-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-movie-id" result))))

        (testing "장르만 업데이트"
          (let [command {:movie-id "test-movie-id"
                        :genres #{:horror :gore}}
                result (use-case/update-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-movie-id" result))))

        (testing "개봉정보만 업데이트"
          (let [command {:movie-id "test-movie-id"
                        :release-info {:release-status :released
                                     :release-date "2024-01-01"}}
                result (use-case/update-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-movie-id" result))))))

    (testing "영화 업데이트 실패"
      (testing "존재하지 않는 영화"
        (with-redefs [movie-repository-fixture/find-by-uuid (constantly nil)]
          (let [command {:movie-id "non-existing-id"
                        :title "새로운 제목"}]
            (is (f/failed? (use-case/update-movie movie-use-case command))))))

      (testing "유효하지 않은 데이터"
        (with-redefs [movie-repository-fixture/find-by-uuid (constantly existing-movie)]
          (testing "빈 제목"
            (let [command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                          :title ""}]
              (is (f/failed? (use-case/update-movie movie-use-case command)))))

          (testing "잘못된 상영시간"
            (let [command {:movie-id "test-movie-id"
                          :runtime -10}]
              (is (f/failed? (use-case/update-movie movie-use-case command)))))

          (testing "필수 장르 누락"
            (let [command {:movie-id "test-movie-id"
                          :genres #{:gore :psychological}}]
              (is (f/failed? (use-case/update-movie movie-use-case command)))))

          (testing "잘못된 개봉일"
            (let [command {:movie-id "test-movie-id"
                          :release-info {:release-status :released
                                       :release-date "2024-13-45"}}]
              (is (f/failed? (use-case/update-movie movie-use-case command))))))))

    (testing "포스터 업데이트"
      (testing "유효한 포스터 파일"
        (with-redefs [movie-repository-fixture/find-by-uuid (constantly existing-movie)
                     movie-repository-fixture/save! (fn [_ movie] movie)
                     image-gateway-fixture/upload (fn [_ file] 
                                                  {:url "http://example.com/uploaded.jpg"
                                                   :width 800
                                                   :height 600})]
          (let [poster-file {:file-name "test.jpg"
                            :file-type "jpg"
                            :file-size 1000000
                            :width 800
                            :height 600
                            :tempfile "temp"}
                command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                        :poster-file poster-file}
                result (use-case/update-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-movie-id" result)))))

      (testing "유효하지 않은 포스터 파일"
        (with-redefs [movie-repository-fixture/find-by-uuid (constantly existing-movie)
                      movie-repository-fixture/save! (fn [_ movie] nil)
                      ]
          
          (testing "지원하지 않는 파일 타입"
            (let [invalid-file {:file-name "test.gif"
                              :file-type "gif"
                              :file-size 1000000
                              :width 800
                              :height 600}
                  command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                          :poster-file invalid-file}]
              (is (f/failed? (use-case/update-movie movie-use-case command)))))

          (testing "파일 크기 초과"
            (let [large-file {:file-name "test.jpg"
                              :file-type "jpg"
                              :file-size (* 20 1024 1024)  ;; 20MB
                              :width 800
                              :height 600}
                  command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                           :poster-file large-file}]
              (is (f/failed? (use-case/update-movie movie-use-case command)))))

          (testing "이미지 크기 초과"
            (let [huge-image {:file-name "test.jpg"
                            :file-type "jpg"
                            :file-size 1000000
                            :width 15000
                            :height 15000}
                  command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                          :poster-file huge-image}]
              (is (f/failed? (use-case/update-movie movie-use-case command))))))))

    (testing "영화 업데이트 성공 - 감독 정보 포함"
      (with-redefs [id-generator-fixture/generate-ulid (constantly "test-id")
                    movie-repository-fixture/find-by-uuid (constantly existing-movie)
                    movie-repository-fixture/save! (fn [_ movie] movie)
                    director-repository-fixture/find-by-name (constantly nil)
                    director-repository-fixture/save! (fn [_ director] director)
                    movie-director-repository-fixture/delete-by-movie-id! (fn [_ _] nil)
                    movie-director-repository-fixture/save-movie-director! 
                    (fn [_ movie-id director-id role]
                      {:movie-id movie-id :director-id director-id :role role})]
        
        (testing "새로운 감독 추가"
          (let [command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                        :director-infos [{:director-name "봉준호" :role "메인 감독"}]}
                result (use-case/update-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-movie-id" result))))
        
        (testing "기존 감독 수정"
          (with-redefs [director-repository-fixture/find-by-name 
                       (constantly {:director-id "existing-director-id"})]
            (let [command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                          :director-infos [{:director-name "봉준호" :role "공동 감독"}]}
                  result (use-case/update-movie movie-use-case command)]
              (is (f/ok? result))
              (is (= "test-movie-id" result))))))) 

    (testing "영화 업데이트 성공 - 극장 정보 포함"
      (with-redefs [id-generator-fixture/generate-ulid (constantly "test-id") 
                    movie-repository-fixture/find-by-uuid (constantly existing-movie)
                    movie-repository-fixture/save! (fn [_ movie] movie)
                    theater-repository-fixture/find-id-by-name (constantly nil)
                    theater-repository-fixture/save! (fn [_ theater] theater)
                    movie-theater-repository-fixture/delete-movie-theater! (fn [_ _ _] nil)
                    movie-theater-repository-fixture/save-movie-theater!
                    (fn [_ _ _] nil)]
        
        (testing "새로운 극장 추가"
          (let [command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                        :theater-infos [{:theater-name "CGV 강남"}
                                      {:theater-name "메가박스 코엑스"}]}
                result (use-case/update-movie movie-use-case command)]
            (is (f/ok? result))
            (is (= "test-movie-id" result))))
        
        (testing "기존 극장 수정"
          (with-redefs [theater-repository-fixture/find-id-by-name 
                       (constantly "existing-theater-id")]
            (let [command {:movie-uuid #uuid "00000000-0000-0000-0000-000000000000"
                          :theater-infos [{:theater-name "CGV 강남"}]}
                  result (use-case/update-movie movie-use-case command)]
              (is (f/ok? result))
              (is (= "test-movie-id" result)))))))))