(ns kit.spooky-town.domain.theater.query.service
  (:require [kit.spooky-town.domain.theater.entity :as entity]
            [kit.spooky-town.domain.theater.repository.protocol :as theater-repo]
            [integrant.core :as ig]))

(defprotocol TheaterQueryService
  (find-theater [this params]
    "극장 상세 정보를 조회합니다.
     params: {:theater-uuid uuid?}")

  (search-theaters [this params]
    "극장 목록을 검색합니다.
     params: {:chain-type keyword?}")

  (get-theater-summary [this params]
    "극장 요약 정보를 조회합니다.
     params: {:theater-uuid uuid?}"))

(defrecord TheaterQueryServiceImpl [theater-repository with-read-only]
  TheaterQueryService
  (find-theater [_ params]
    (with-read-only [theater-repository]
      (fn [repo]
        (theater-repo/find-by-uuid repo (:theater-uuid params)))))

  (search-theaters [_ params]
    (with-read-only [theater-repository]
      (fn [repo]
        (theater-repo/find-by-chain-type repo (:chain-type params)))))

  (get-theater-summary [_ params]
    (with-read-only [theater-repository]
      (fn [repo]
        (when-let [theater (theater-repo/find-by-uuid repo (:theater-uuid params))]
          (entity/->summary theater))))))

(defmethod ig/init-key :domain/theater-query-service
  [_ {:keys [theater-repository with-read-only]}]
  (->TheaterQueryServiceImpl theater-repository with-read-only)) 