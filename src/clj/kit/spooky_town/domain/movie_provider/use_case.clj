(ns kit.spooky-town.domain.movie-provider.use-case
  (:require [integrant.core :as ig]
            [failjure.core :as f]
            [kit.spooky-town.domain.movie-provider.entity :as entity]
            [kit.spooky-town.domain.movie-provider.repository.protocol :as movie-provider-repo]
            [kit.spooky-town.domain.movie.repository.protocol :as movie-repo]
            [kit.spooky-town.domain.common.id.protocol :as id-generator]
            [kit.spooky-town.domain.auth.authorization.protocol :refer [has-permission?]]))

(defprotocol MovieProviderUseCase
  (assign-provider! [this command]
    "영화에 OTT 플랫폼을 연결합니다.
     command: {:movie-uuid string?
               :provider-id string?
               :user-uuid string?}")
  
  (remove-provider! [this command]
    "영화에서 OTT 플랫폼 연결을 제거합니다.
     command: {:movie-uuid string?
               :provider-id string?
               :user-uuid string?}")
  
  (get-providers [this command]
    "영화에 연결된 모든 OTT 플랫폼을 조회합니다.
     command: {:movie-uuid string?}")
  
  (get-movies-by-provider [this command]
    "OTT 플랫폼에 연결된 모든 영화를 조회합니다.
     command: {:provider-id string?}"))

(defrecord MovieProviderUseCaseImpl [movie-provider-repository 
                                   movie-repository
                                   user-authorization
                                   id-generator
                                   uuid-generator
                                   with-tx]
  MovieProviderUseCase
  (assign-provider! [_ {:keys [movie-uuid provider-id user-uuid]}]
    (if (has-permission? user-authorization user-uuid :content-manager)
      (with-tx [movie-provider-repository]
        (fn [repo]
          (if-let [movie-id (movie-repo/find-id-by-uuid movie-repository movie-uuid)]
            (if-let [movie-provider (entity/create-movie-provider
                                     
                                    {:movie-provider-id (id-generator/generate-ulid id-generator)
                                     :uuid (id-generator/generate-uuid uuid-generator)
                                     :movie-id movie-id
                                     :provider-id provider-id
                                     :created-at (java.util.Date.)})]
              (movie-provider-repo/save! repo movie-provider)
              (f/fail "유효하지 않은 영화-OTT 플랫폼 연결입니다."))
            (f/fail "영화를 찾을 수 없습니다."))))
      (f/fail "OTT 플랫폼을 연결할 권한이 없습니다.")))

  (remove-provider! [_ {:keys [movie-uuid provider-id user-uuid]}]
    (if (has-permission? user-authorization user-uuid :content-manager)
      (with-tx [movie-provider-repository]
        (fn [repo]
          (if-let [movie-id (movie-repo/find-id-by-uuid movie-repository movie-uuid)]
            (movie-provider-repo/delete! repo movie-id provider-id)
            (f/fail "영화를 찾을 수 없습니다."))))
      (f/fail "OTT 플랫폼 연결을 제거할 권한이 없습니다.")))

  (get-providers [_ {:keys [movie-uuid]}]
    (when-let [movie-id (movie-repo/find-id-by-uuid movie-repository movie-uuid)]
      (movie-provider-repo/find-by-movie movie-provider-repository movie-id)))

  (get-movies-by-provider [_ {:keys [provider-id]}]
    (movie-provider-repo/find-by-provider movie-provider-repository provider-id)))

(defmethod ig/init-key :domain/movie-provider-use-case
  [_ {:keys [movie-provider-repository 
             movie-repository 
             user-authorization 
             id-generator
             uuid-generator
             with-tx]}]
  (->MovieProviderUseCaseImpl movie-provider-repository 
                             movie-repository
                             user-authorization 
                             id-generator
                             uuid-generator
                             with-tx)) 