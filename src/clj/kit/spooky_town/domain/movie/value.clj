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

;; 장르 (필수로 호러나 스릴러를 포함해야 함)
(s/def ::primary-genre #{:horror :thriller})
(s/def ::secondary-genre #{:psychological   ;; 심리
                          :supernatural    ;; 초자연
                          :slasher        ;; 슬래셔
                          :zombie         ;; 좀비
                          :monster        ;; 몬스터
                          :gore           ;; 고어
                          :mystery})      ;; 미스터리

(s/def ::genres (s/and (s/coll-of (s/or :primary ::primary-genre
                                        :secondary ::secondary-genre)
                                  :kind set?)
                       #(some ::primary-genre %)))  ;; horror나 thriller 중 하나는 반드시 포함

;; 기본 생성 함수들
(defn create-title [title]
  (when (s/valid? ::title title)
    title))

(defn create-directors [directors]
  (when (s/valid? ::directors directors)
    directors))

(defn create-genres [genres]
  (when (s/valid? ::genres genres)
    genres)) 