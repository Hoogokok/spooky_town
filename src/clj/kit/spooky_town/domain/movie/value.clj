(ns kit.spooky-town.domain.movie.value
  (:require [clojure.spec.alpha :as s]))

;; 제목 (필수)
(def max-title-length 200)
(s/def ::title (s/and string? 
                      #(pos? (count %))
                      #(<= (count %) max-title-length)))

;; 감독 (필수)
(def max-director-name-length 20)
(s/def ::director-name (s/and string? 
                             #(pos? (count %))
                             #(<= (count %) max-director-name-length)))
(s/def ::director (s/keys :req-un [::director-name]))
(s/def ::directors (s/and (s/coll-of ::director :kind vector?)
                         #(pos? (count %))))  ;; 최소 1명 이상

;; 기본 생성 함수들
(defn create-title [title]
  (when (s/valid? ::title title)
    title))

(defn create-directors [directors]
  (when (s/valid? ::directors directors)
    directors)) 