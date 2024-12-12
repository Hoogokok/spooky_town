(ns kit.spooky-town.domain.movie-provider.use-case-test
  (:require [clojure.test :refer :all]
            [failjure.core :as f]
            [clojure.tools.logging :as log]
            [kit.spooky-town.domain.movie-provider.use-case :as sut]
            [kit.spooky-town.domain.movie-provider.test.repository :refer [->TestMovieProviderRepository]]
            [kit.spooky-town.domain.movie.test.repository :refer [->TestMovieRepository]]
            [kit.spooky-town.domain.auth.test.authorization :refer [->TestUserAuthorization]]
            [kit.spooky-town.domain.common.id.test.generator :refer [->TestIdGenerator]]
            [kit.spooky-town.domain.common.id.test.uuid-generator :refer [->TestUuidGenerator]]))

(deftest movie-provider-use-case-test
  (let [with-tx (fn [repos f] (apply f repos))
        movie-provider-repository (->TestMovieProviderRepository)
        movie-repository (->TestMovieRepository)
        user-authorization (->TestUserAuthorization)
        id-generator (->TestIdGenerator)
        uuid-generator (->TestUuidGenerator)
        use-case (sut/->MovieProviderUseCaseImpl
                  movie-provider-repository
                  movie-repository
                  user-authorization
                  id-generator
                  uuid-generator
                  with-tx)
        
        ;; 테스트 데이터
        movie-uuid #uuid "550e8400-e29b-41d4-a716-446655440000"
        movie-id "01HQ1234567890ABCDEFGHJKLM"
        provider-id "NETFLIX"
        user-uuid #uuid "550e8400-e29b-41d4-a716-446655440001"
        
        ;; Mock functions
        save!-fn (atom nil)
        delete!-fn (atom nil)
        find-by-movie-fn (atom nil)
        find-by-provider-fn (atom nil)
        find-id-by-uuid-fn (atom nil)
        has-permission?-fn (atom nil)
        generate-ulid-fn (atom nil)
        generate-uuid-fn (atom nil)]
    
    (with-redefs [kit.spooky-town.domain.movie-provider.test.repository/save! 
                  (fn [_ provider] 
                    (log/info "Saving provider:" provider)
                    (@save!-fn provider))
                  
                  kit.spooky-town.domain.movie-provider.test.repository/delete! 
                  (fn [_ movie-id provider-id]
                    (log/info "Deleting provider:" {:movie-id movie-id :provider-id provider-id}) 
                    (@delete!-fn movie-id provider-id))
                  
                  kit.spooky-town.domain.movie-provider.test.repository/find-by-movie 
                  (fn [_ movie-id]
                    (log/info "Finding by movie:" movie-id)
                    (@find-by-movie-fn movie-id))
                  
                  kit.spooky-town.domain.movie-provider.test.repository/find-by-provider 
                  (fn [_ provider-id]
                    (log/info "Finding by provider:" provider-id)
                    (@find-by-provider-fn provider-id))
                  
                  kit.spooky-town.domain.movie.test.repository/find-id-by-uuid
                  (fn [_ uuid]
                    (log/info "Finding movie id by uuid:" uuid)
                    (@find-id-by-uuid-fn uuid))
                  
                  kit.spooky-town.domain.auth.test.authorization/has-permission?
                  (fn [_ user-uuid permission]
                    (log/info "Checking permission:" {:user-uuid user-uuid :permission permission})
                    (@has-permission?-fn user-uuid permission))
                  
                  kit.spooky-town.domain.common.id.test.generator/generate-ulid
                  (fn [_]
                    (log/info "Generating ULID")
                    (@generate-ulid-fn))
                  
                  kit.spooky-town.domain.common.id.test.uuid-generator/generate-uuid
                  (fn [_]
                    (log/info "Generating UUID")
                    (@generate-uuid-fn))]
      
      (testing "OTT 플랫폼 연결"
        (testing "성공 케이스"
          (reset! has-permission?-fn (constantly true))
          (reset! find-id-by-uuid-fn (constantly movie-id))
          (reset! generate-ulid-fn (constantly "01HQ1234567890ABCDEFGHJKLM"))
          (reset! generate-uuid-fn (constantly #uuid "550e8400-e29b-41d4-a716-446655440000"))
          (reset! save!-fn identity)
          
          (let [result (sut/assign-provider! 
                        use-case 
                        {:movie-uuid movie-uuid
                         :provider-id provider-id
                         :user-uuid user-uuid})]
            (log/info "Result:" result)
            (is (not (f/failed? result)))))
        
        (testing "권한이 없는 경우"
          (reset! has-permission?-fn (constantly false))
          
          (let [result (sut/assign-provider! 
                        use-case 
                        {:movie-uuid movie-uuid
                         :provider-id provider-id
                         :user-uuid user-uuid})]
            (is (f/failed? result))
            (is (= "OTT 플랫폼을 연결할 권한이 없습니다." (f/message result)))))
        
        (testing "존재하지 않는 영화"
          (reset! has-permission?-fn (constantly true))
          (reset! find-id-by-uuid-fn (constantly nil))
          
          (let [result (sut/assign-provider! 
                        use-case 
                        {:movie-uuid movie-uuid
                         :provider-id provider-id
                         :user-uuid user-uuid})]
            (is (f/failed? result))
            (is (= "영화를 찾을 수 없습니다." (f/message result))))))
      
      (testing "OTT 플랫폼 연결 해제"
        (testing "성공 ���이스"
          (reset! has-permission?-fn (constantly true))
          (reset! find-id-by-uuid-fn (constantly movie-id))
          (reset! delete!-fn (constantly true))
          
          (let [result (sut/remove-provider! 
                        use-case 
                        {:movie-uuid movie-uuid
                         :provider-id provider-id
                         :user-uuid user-uuid})]
            (is (not (f/failed? result)))))
        
        (testing "권한이 없는 경우"
          (reset! has-permission?-fn (constantly false))
          
          (let [result (sut/remove-provider! 
                        use-case 
                        {:movie-uuid movie-uuid
                         :provider-id provider-id
                         :user-uuid user-uuid})]
            (is (f/failed? result))
            (is (= "OTT 플랫폼 연결을 제거할 권한이 없습니다." (f/message result))))))
      
      (testing "영화의 OTT 플랫폼 조회"
        (reset! find-id-by-uuid-fn (constantly movie-id))
        (reset! find-by-movie-fn 
                (constantly [{:provider-id "NETFLIX" :name "Netflix"}]))
        
        (let [result (sut/get-providers 
                      use-case 
                      {:movie-uuid movie-uuid})]
          (is (= [{:provider-id "NETFLIX" :name "Netflix"}] result))))
      
      (testing "OTT 플랫폼의 영화 조회"
        (reset! find-by-provider-fn 
                (constantly [{:movie-id movie-id :title "스크림"}]))
        
        (let [result (sut/get-movies-by-provider 
                      use-case 
                      {:provider-id provider-id})]
          (is (= [{:movie-id movie-id :title "스크림"}] result))))))) 