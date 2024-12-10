(ns kit.spooky-town.domain.theater.value
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

;; ID 값 객체
(s/def ::theater-id (s/and string? #(not (str/blank? %))))
(s/def ::uuid uuid?)

;; 극장 체인 값 객체 (영화 배급사)
(s/def ::chain-type #{:cgv :megabox :lotte})

;; 타임스탬프
(s/def ::created-at inst?)
(s/def ::updated-at inst?)

;; 값 객체 생성 함수들
(defn create-chain-type [chain]
  (when (s/valid? ::chain-type chain)
    chain))

(defn create-timestamp []
  (java.util.Date.))

;; 유틸리티 함수
(defn chain-type->str [chain-type]
  (case chain-type
    :cgv "CGV"
    :megabox "메가박스"
    :lotte "롯데시네마"
    "알 수 없음")) 