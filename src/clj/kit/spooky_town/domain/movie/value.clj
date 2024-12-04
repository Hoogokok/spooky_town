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

;; 개봉 상태와 날짜
(s/def ::release-status #{:released        ;; 개봉됨
                         :upcoming         ;; 개봉 예정
                         :unknown})        ;; 미정

(s/def ::release-date inst?)              ;; 실제 개봉일 또는 개봉 예정일
(s/def ::release-info 
  (s/keys :req-un [::release-status]
          :opt-un [::release-date]))

(defn create-release-info
  ([status]
   (when (s/valid? ::release-status status)
     {:release-status status}))
  ([status date]
   (when (and (s/valid? ::release-status status)
              (s/valid? ::release-date date))
     {:release-status status
      :release-date date})))

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