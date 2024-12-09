(ns kit.spooky-town.domain.theater.entity
  (:require [clojure.spec.alpha :as s]
            [kit.spooky-town.domain.theater.value :as value]))

;; 엔티티 스펙
(s/def ::theater
  (s/keys :req-un [::value/theater-id
                   ::value/uuid
                   ::value/chain-type
                   ::value/created-at
                   ::value/updated-at]))

;; 엔티티 레코드
(defrecord Theater [theater-id
                    uuid
                    chain-type
                    created-at
                    updated-at])

;; 생성 함수
(defn create-theater [{:keys [theater-id
                              uuid
                              chain-type
                              created-at]
                       :or {created-at (value/create-timestamp)}}]
  (when-let [validated-chain (value/create-chain-type chain-type)]
    (let [theater (map->Theater
                   {:theater-id theater-id
                    :uuid uuid
                    :chain-type validated-chain
                    :created-at created-at
                    :updated-at created-at})]
      (when (s/valid? ::theater theater)
        theater))))

;; 요약 정보 변환
(defn ->summary [{:keys [uuid chain-type]}]
  {:uuid uuid
   :chain-name (value/chain-type->str chain-type)})

;; 유효성 검사
(defn valid? [theater]
  (s/valid? ::theater theater))

;; 업데이트 함수
(defn update-theater [theater {:keys [chain-type]}]
  (when-let [validated-chain (value/create-chain-type chain-type)]
    (let [updated-theater (assoc theater
                                :chain-type validated-chain
                                :updated-at (value/create-timestamp))]
      (when (valid? updated-theater)
        updated-theater)))) 