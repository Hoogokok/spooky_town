(ns kit.spooky-town.domain.movie.use-case
  (:require [integrant.core :as ig]
            [failjure.core :as f] 
            [kit.spooky-town.domain.movie.value :as value]
            [kit.spooky-town.domain.director.repository.protocol :as director-repository]
            [kit.spooky-town.domain.common.image.gateway.protocol :as image-gateway]
            [kit.spooky-town.domain.common.id.protocol :as id-generator]
            [kit.spooky-town.domain.movie-director.repository.protocol :as movie-director-repository]
            [kit.spooky-town.domain.movie-actor.repository.protocol :as movie-actor-repository]
            [kit.spooky-town.domain.actor.repository.protocol :as actor-repository]
            [kit.spooky-town.domain.movie.repository.protocol :as movie-repository]))

(defprotocol MovieUseCase
  (create-movie [this command]
    "새로운 영화를 생성합니다."))

(defrecord CreateMovieCommand [title director-names release-info genres movie-actor-infos runtime poster-file])

(defrecord CreateMovieUseCaseImpl [with-tx
                                   movie-repository
                                   movie-director-repository
                                   movie-actor-repository
                                   director-repository
                                   actor-repository
                                   image-gateway
                                   id-generator]
  MovieUseCase
  (create-movie [_ {:keys [title director-infos release-info genres
                          runtime movie-actor-infos poster-file] :as command}]
    (with-tx movie-repository
      (fn [repo]
        ;; 필수 필드 검증
        (if (and (value/create-title title)
                 (value/create-director-inputs
                  (mapv #(hash-map :director-name (:director-name %)
                                   :director-role (:role %))
                        director-infos))
                 (value/create-release-info release-info)
                 (value/create-genres genres))
          ;; 검증 성공 시 기본 필드 추가
          (let [movie-id (id-generator/generate-ulid id-generator)
                created-at (java.util.Date.)
                updated-at created-at
                validated-runtime (when runtime
                                  (value/create-runtime runtime))
                poster (when poster-file
                        (when-let [uploaded (image-gateway/upload image-gateway poster-file)]
                          (value/create-poster uploaded)))]
            
            ;; 영화 저장
            (movie-repository/save! repo 
              (cond-> {:movie-id movie-id
                      :title title
                      :release-info release-info
                      :genres genres
                      :uuid (random-uuid)
                      :created-at created-at
                      :updated-at updated-at}
                
                validated-runtime
                (assoc :runtime validated-runtime)
                
                poster
                (assoc :poster poster)))
            
            ;; 감독 관계 저장
            (doseq [{:keys [director-name role]} director-infos]
              (let [director-id (or
                                (:director-id (director-repository/find-by-name director-repository director-name))
                                (:director-id
                                  (director-repository/save! director-repository
                                                         {:director-id (id-generator/generate-ulid id-generator)
                                                          :director-name director-name})))]
                (movie-director-repository/save-movie-director! movie-director-repository movie-id director-id role)))
            
            ;; 배우 관계 저장
            (doseq [{:keys [actor-name role]} movie-actor-infos]
              (let [actor-id (or
                             (:actor-id (actor-repository/find-by-name actor-repository actor-name))
                             (:actor-id
                               (actor-repository/save! actor-repository
                                                    {:actor-id (id-generator/generate-ulid id-generator)
                                                     :actor-name actor-name})))]
                (movie-actor-repository/save-movie-actor! movie-actor-repository movie-id actor-id role)))
            
            ;; 생성된 영화 ID 반환
            movie-id)
          ;; 검증 실패 시
          (f/fail "필수 필드가 유효하지 않습니다."))))))

(defmethod ig/init-key :domain/movie-use-case [_ {:keys [with-tx movie-repository movie-director-repository movie-actor-repository director-repository actor-repository image-gateway id-generator]}]
  (->CreateMovieUseCaseImpl with-tx movie-repository movie-director-repository movie-actor-repository director-repository actor-repository image-gateway id-generator))