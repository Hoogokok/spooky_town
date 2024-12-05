(ns kit.spooky-town.domain.movie.use-case
  (:require [integrant.core :as ig]
            [failjure.core :as f]
            [kit.spooky-town.domain.movie.entity :as entity]
            [kit.spooky-town.domain.movie.value :as value]
            [kit.spooky-town.domain.director.repository.protocol :as director-repository]
            [kit.spooky-town.domain.common.image.gateway.protocol :as image-gateway]
            [kit.spooky-town.domain.common.id.protocol :as id-generator]
            [kit.spooky-town.domain.actor.repository.protocol :as actor-repository]
            [kit.spooky-town.domain.movie.repository.protocol :as movie-repository]))

(defprotocol MovieUseCase
  (create-movie [this command]
    "새로운 영화를 생성합니다."))

(defrecord CreateMovieCommand [title director-names release-info genres movie-actor-infos runtime poster-file])

(defrecord CreateMovieUseCaseImpl [with-tx
                                   movie-repository
                                   director-repository
                                   actor-repository
                                   image-gateway
                                   id-generator]
  MovieUseCase
  (create-movie [_ {:keys [title director-names release-info genres] :as command}]
    (with-tx movie-repository
      (fn [repo]
        ;; 필수 필드 검증
        (if (and (value/create-title title)
                 (value/create-director-inputs (mapv #(hash-map :director-name %) director-names))
                 (value/create-release-info release-info)
                 (value/create-genres genres))
          ;; 검증 성공 시 감독 처리 및 기본 필드 추가
          (let [director-ids (mapv
                             (fn [director-name]
                               (or
                                 (:director-id (director-repository/find-by-name director-repository director-name))
                                 (:director-id
                                   (director-repository/save! director-repository
                                                           {:director-id (id-generator/generate-ulid id-generator)
                                                            :director-name director-name}))))
                             director-names)
                movie-id (id-generator/generate-ulid id-generator)
                created-at (java.util.Date.)
                updated-at created-at]
            (movie-repository/save! repo 
              (-> command
                  (assoc :movie-id movie-id
                         :director-ids director-ids
                         :uuid (random-uuid)
                         :created-at created-at
                         :updated-at updated-at))))
          ;; 검증 실패 시
          (f/fail "필수 필드가 유효하지 않습니다."))))))

(defmethod ig/init-key ::create-movie-use-case [_ {:keys [with-tx movie-repository director-repository actor-repository image-gateway id-generator]}]
  (->CreateMovieUseCaseImpl with-tx movie-repository director-repository actor-repository image-gateway id-generator))