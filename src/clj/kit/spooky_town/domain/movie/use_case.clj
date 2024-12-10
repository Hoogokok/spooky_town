(ns kit.spooky-town.domain.movie.use-case
  (:require
   [failjure.core :as f]
   [integrant.core :as ig]
   [kit.spooky-town.domain.actor.repository.protocol :as actor-repository]
   [kit.spooky-town.domain.common.id.protocol :as id-generator]
   [kit.spooky-town.domain.common.image.gateway.protocol :as image-gateway]
   [kit.spooky-town.domain.director.repository.protocol :as director-repository]
   [kit.spooky-town.domain.movie-actor.repository.protocol :as movie-actor-repository]
   [kit.spooky-town.domain.movie-director.repository.protocol :as movie-director-repository]
   [kit.spooky-town.domain.movie.entity :as entity]
   [kit.spooky-town.domain.movie.repository.protocol :as movie-repository]
   [kit.spooky-town.domain.movie-theater.repository.protocol :as movie-theater-repository]
   [kit.spooky-town.domain.theater.repository.protocol :as theater-repository]
   [kit.spooky-town.domain.movie.value :as value]))


  (defprotocol MovieUseCase
    (create-movie [this command]
      "새로운 영화를 생성합니다.")
    (update-movie [this command]
      "영화 정보를 업데이트합니다."))

  (defrecord CreateMovieCommand [title director-infos release-info genres
                                 movie-actor-infos runtime poster-file
                                 theater-infos])

  (defrecord UpdateMovieCommand [movie-uuid
                                 title
                                 runtime
                                 genres
                                 release-info
                                 poster-file
                                 director-infos
  theater-infos])

  (defn- process-poster [image-gateway poster-file]
    (when poster-file
      (if-let [validated-file (value/create-poster-file poster-file)]
        (when-let [uploaded (image-gateway/upload image-gateway validated-file)]
          (value/create-poster uploaded))
        (f/fail "유효하지 않은 포스터 파일입니다."))))

  (defn- process-theater [theater-repository id-generator {:keys [theater-name]}]
  (let [theater-id (or
                    (theater-repository/find-id-by-name theater-repository theater-name)
                    (:theater-id
                     (theater-repository/save! theater-repository
                                               {:theater-id (id-generator/generate-ulid id-generator)
                                                :theater-name theater-name})))]
    {:theater-id theater-id}))

(defn- process-director [director-repository id-generator {:keys [director-name role]}]
  (let [director-id (or
                     (:director-id (director-repository/find-by-name director-repository director-name))
                     (:director-id
                      (director-repository/save! director-repository
                                                 {:director-id (id-generator/generate-ulid id-generator)
                                                  :director-name director-name})))]
    {:director-id director-id :role role}))

(defn- prepare-base-update-data [{:keys [title runtime genres release-info] :as command} poster-result]
  (if (f/failed? poster-result)
    poster-result
    (cond-> (if poster-result
              (-> command
                  (assoc :poster poster-result)
                  (dissoc :poster-file))
              (dissoc command :poster-file))
      title (assoc :title title)
      runtime (assoc :runtime runtime)
      genres (assoc :genres genres)
      release-info (assoc :release-info release-info))))

(defn- update-director-info [movie-director-repository director-repository id-generator movie-id director-infos]
  (when director-infos
    (movie-director-repository/delete-by-movie-id! movie-director-repository movie-id)
    (doseq [{:keys [director-id role]} (map #(process-director director-repository id-generator %) director-infos)]
      (movie-director-repository/save-movie-director! movie-director-repository movie-id director-id role))))

(defn- update-theater-info [movie-theater-repository theater-repository id-generator movie-id theater-infos]
  (when theater-infos
    (doseq [{:keys [theater-id]} (map #(process-theater theater-repository id-generator %) theater-infos)]
      (movie-theater-repository/delete-movie-theater! movie-theater-repository movie-id theater-id)
      (movie-theater-repository/save-movie-theater! movie-theater-repository movie-id theater-id))))

  (defrecord CreateMovieUseCaseImpl [with-tx
                                     movie-repository
                                     movie-director-repository
                                     movie-actor-repository
                                     movie-theater-repository
                                     director-repository
                                     actor-repository
                                     theater-repository
                                     image-gateway
                                     id-generator
                                     uuid-generator]
    MovieUseCase
    (create-movie [_ {:keys [title director-infos release-info genres
                             runtime movie-actor-infos poster-file
                             theater-infos] :as command}]
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
                  movie-uuid (id-generator/generate-uuid uuid-generator)
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
                                               :uuid movie-uuid
                                               :title title
                                               :release-info release-info
                                               :genres genres
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

            ;; 극장 관계 저장
              (doseq [{:keys [theater-name]} theater-infos]
                (let [theater-id (or
                                  (theater-repository/find-id-by-name theater-repository theater-name)
                                  (:theater-id
                                   (theater-repository/save! theater-repository
                                                             {:theater-id (id-generator/generate-ulid id-generator)
                                                              :theater-name theater-name})))]
                  (movie-theater-repository/save-movie-theater!
                   movie-theater-repository movie-id theater-id)))

            ;; 생성된 영화 ID 반환
              movie-id)
          ;; 검증 실패 시
            (f/fail "필수 필드가 유효하지 않습니다.")))))

    (update-movie [_ {:keys [movie-uuid] :as command}]
      (with-tx movie-repository
        (fn [repo]
          (if-let [movie (movie-repository/find-by-uuid repo movie-uuid)]
            (let [movie-id (:movie-id movie)
                  poster-result (process-poster image-gateway (:poster-file command))
                  update-data (prepare-base-update-data command poster-result)]
              (if (f/failed? update-data)
                update-data
                (if-let [updated-movie (entity/update-movie movie update-data)]
                  (do
                    (movie-repository/save! repo updated-movie)
                    (update-director-info movie-director-repository director-repository id-generator movie-id (:director-infos command))
                    (update-theater-info movie-theater-repository theater-repository id-generator movie-id (:theater-infos command))
                    movie-id)
                  (f/fail "영화 정보 업데이트가 유효하지 않습니다."))))
            (f/fail "영화를 찾을 수 없습니다."))))))

  (defmethod ig/init-key :domain/movie-use-case [_ {:keys [with-tx movie-repository movie-director-repository movie-actor-repository movie-theater-repository director-repository actor-repository theater-repository image-gateway id-generator uuid-generator]}]
    (->CreateMovieUseCaseImpl with-tx movie-repository movie-director-repository movie-actor-repository movie-theater-repository director-repository actor-repository theater-repository image-gateway id-generator uuid-generator))