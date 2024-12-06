(ns kit.spooky-town.domain.movie.value
  (:require [clojure.spec.alpha :as s]
            [kit.spooky-town.domain.common.image :as image]
            [clojure.string :as str]))
;; movie-id
(s/def ::movie-id (s/and string? #(not (str/blank? %))))

(defn create-movie-id [id]
  (when (s/valid? ::movie-id id)
    id))

;; created-at
(s/def ::created-at inst?)

(defn create-created-at [created-at]
  (when (s/valid? ::created-at created-at)
    created-at))

;; updated-at
(s/def ::updated-at inst?)

(defn create-updated-at [updated-at]
  (when (s/valid? ::updated-at updated-at)
    updated-at))

;; 제목 (필수)
(def max-title-length 200)
(s/def ::title (s/and string?
                      #(pos? (count %))
                      #(<= (count %) max-title-length)))

;; 감독 (필수)
;; 입력용 값 객체
(def max-director-name-length 20)
(s/def ::director-name (s/and string?
                             #(pos? (count %))
                             #(<= (count %) max-director-name-length)))
(def max-director-role-length 20)
(s/def ::director-role (s/and string?
                             #(pos? (count %))
                             #(<= (count %) max-director-role-length)))
(s/def ::director-input (s/keys :req-un [::director-name ::director-role]))
(s/def ::director-inputs (s/and (s/coll-of ::director-input :kind vector?)
                               #(pos? (count %))))

;; 저장용 값 객체
(s/def ::director-id (s/and string? #(not (str/blank? %))))
(s/def ::director-ids (s/and (s/coll-of ::director-id :kind vector?)
                             #(pos? (count %))))

(defn create-director-input [director]
  (when (s/valid? ::director-input director)
    director))

(defn create-director-inputs [directors]
  (when (s/valid? ::director-inputs directors)
    directors))

(defn create-director-ids [ids]
  (when (and (vector? ids)  ;; 벡터인지 확인
             (every? string? ids)  ;; 모든 요소가 문자열인지 확인
             (s/valid? ::director-ids ids))
    ids))

;; 장르 (필수로 호러나 스릴러를 포함해야 함)
(s/def ::primary-genre #{:horror :thriller})
(s/def ::secondary-genre #{:psychological   ;; 심리
                           :supernatural    ;; 초자연
  :slasher        ;; 슬래셔
  :zombie         ;; 좀비
  :monster        ;; 몬스터
  :gore           ;; 고어
  :mystery})      ;; 미스터리

(s/def ::genres (s/and (s/coll-of keyword? :kind set?)
                       #(or (contains? % :horror)
                            (contains? % :thriller))
                       #(every? (fn [genre] (or (contains? #{:horror :thriller} genre)
                                                (contains? #{:psychological :supernatural :slasher
                  :zombie :monster :gore :mystery} genre))) %)))

;; 개봉 상태와 날짜
(s/def ::release-status #{:released        ;; 개봉됨
                          :upcoming         ;; 개봉 예정
  :unknown})        ;; 미정

(def date-regex #"^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$")
(s/def ::release-date (s/and string? #(re-matches date-regex %)))
(s/def ::release-info
  (s/keys :req-un [::release-status]
          :opt-un [::release-date]))

(defn create-release-info [{:keys [release-status release-date] :as release-info}]
  (when (s/valid? ::release-info release-info)
    release-info))

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

;; 상영시간 (분 단위, 양수)
(s/def ::runtime (s/and pos-int? #(pos? %)))

(defn create-runtime [runtime]
  (when (and (integer? runtime)  ;; 정수인지 확인
             (pos? runtime)      ;; 양수인지 확인
             (s/valid? ::runtime runtime))
    runtime))

;; 배우 정보
;; 입력용 값 객체
(def max-actor-name-length 50)
(s/def ::actor-name (s/and string?
                           #(pos? (count %))
                           #(<= (count %) max-actor-name-length)))
(s/def ::role string?)
(s/def ::actor-input (s/keys :req-un [::actor-name]
                             :opt-un [::role]))
(s/def ::actor-inputs (s/coll-of ::actor-input :kind vector?))

;; 저장용 값 객체
(s/def ::actor-id (s/and string? #(not (str/blank? %))))
(s/def ::movie-actor {:actor-id ::actor-id
                      :role (s/nilable ::role)})
(s/def ::movie-actors (s/coll-of ::movie-actor :kind vector?))

(defn create-actor-input [actor]
  (when (s/valid? ::actor-input actor)
    actor))

(defn create-actor-inputs [actors]
  (when (s/valid? ::actor-inputs actors)
    actors))

(defn create-movie-actor [{:keys [actor-id] :as actor}]
  (when (and (string? actor-id)  ;; actor-id가 문자열인지 확인
             (s/valid? ::movie-actor actor))
    actor))

(defn create-movie-actors [actors]
  (when (and (vector? actors)  ;; 벡터인지 확인
             (every? #(and (map? %)  ;; 각 요소가 맵이고
                          (string? (:actor-id %)))  ;; actor-id가 문자열인지 확인
             actors)
             (s/valid? ::movie-actors actors))
    actors))

;; 포스터 (이미지 값 객체 사용)
(s/def ::poster ::image/image)

(defn create-poster [poster]
  (image/create-image poster))

(s/def ::movie-director
  (s/keys :req-un [::director-id ::role]))

(s/def ::movie-directors
  (s/coll-of ::movie-director :kind vector?))
  