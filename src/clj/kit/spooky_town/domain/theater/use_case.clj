(ns kit.spooky-town.domain.theater.use-case
  (:require [kit.spooky-town.domain.theater.entity :as entity]
            [kit.spooky-town.domain.common.id.protocol :as id-generator]
            [kit.spooky-town.domain.theater.repository.protocol :as theater-repo]
            [integrant.core :as ig]))

(defprotocol TheaterUseCase
  (create-theater! [this command]
    "새로운 극장을 생성합니다.
     command: {:chain-type keyword?}")

  (update-theater! [this command]
    "극장 정보를 수정합니다.
     command: {:theater-uuid uuid?
               :chain-type keyword?}")

  (delete-theater! [this command]
    "극장을 삭제합니다.
     command: {:theater-uuid uuid?}"))

(defrecord TheaterUseCaseImpl [theater-repository id-generator uuid-generator with-tx]
  TheaterUseCase
  (create-theater! [_ command]
    (with-tx [theater-repository]
      (fn [repo]
        (when-let [theater (entity/create-theater
                            {:theater-id (id-generator/generate-ulid id-generator)
                             :uuid (id-generator/generate-uuid uuid-generator)
                             :chain-type (:chain-type command)})]
          (theater-repo/save! repo theater)))))

  (update-theater! [_ command]
    (with-tx [theater-repository]
      (fn [repo]
        (when-let [theater (theater-repo/find-by-uuid repo (:theater-uuid command))]
          (when-let [updated-theater (entity/update-theater
                                     theater
                                     {:chain-type (:chain-type command)})]
            (theater-repo/save! repo updated-theater))))))

  (delete-theater! [_ command]
    (with-tx [theater-repository]
      (fn [repo]
        (when-let [theater (theater-repo/find-by-uuid repo (:theater-uuid command))]
          (theater-repo/delete! repo theater))))))

(defmethod ig/init-key :domain/theater-use-case
  [_ {:keys [theater-repository id-generator uuid-generator with-tx]}]
  (->TheaterUseCaseImpl theater-repository id-generator uuid-generator with-tx)) 