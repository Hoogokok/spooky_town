(ns kit.spooky-town.domain.movie-theater.value
  (:require [clojure.spec.alpha :as s]))

;; ID 값 객체
(s/def ::movie-id string?)
(s/def ::theater-id string?)

;; 타임스탬프
(s/def ::created-at inst?)
(s/def ::updated-at inst?)

;; 값 객체 생성 함수
(defn create-timestamp []
  (java.util.Date.)) 