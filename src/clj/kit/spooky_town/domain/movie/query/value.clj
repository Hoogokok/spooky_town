(ns kit.spooky-town.domain.movie.query.value
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

;; 페이지네이션
(def max-per-page 12)
(s/def ::page pos-int?)
(s/def ::total-count nat-int?)
(s/def ::total-pages pos-int?)

;; 정렬 조건
(s/def ::sort-by #{:title :release-date :created-at :release-status})
(s/def ::sort-order #{:asc :desc})

;; 검색 조건
(s/def ::title (s/and string? #(<= (count %) 200)))
(s/def ::director-name (s/and string? #(<= (count %) 20)))
(s/def ::actor-name (s/and string? #(<= (count %) 50)))
(s/def ::release-date (s/and string? #(re-matches #"^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$" %)))

;; 장르 필터
(s/def ::primary-genre #{:horror :thriller})
(s/def ::secondary-genre #{:psychological :supernatural :slasher :zombie :monster :gore :mystery})
(s/def ::genres (s/and (s/coll-of keyword? :kind set?)
                      #(or (contains? % :horror)
                           (contains? % :thriller))))

;; 개봉 상태
(s/def ::release-status #{:released :upcoming :unknown})
(def release-status-priority
  {:released 1
   :upcoming 2
   :unknown 3})

;; 검색 결과 요약
(s/def ::movie-summary
  (s/keys :req-un [::movie-id
                   ::title
                   ::poster-url
                   ::release-date
                   ::genres
                   ::director-names
                   ::release-status]))

;; 페이지 결과
(s/def ::movie-page-result
  (s/keys :req-un [::page
                   ::total-count
                   ::total-pages
                   ::movies]))

;; 검색 조건
(s/def ::movie-search-criteria
  (s/keys :req-un [::page]
          :opt-un [::sort-by 
                   ::sort-order
                   ::title 
                   ::director-name
                   ::actor-name
                   ::release-date
                   ::genres
                   ::release-status]))

;; 생성 함수들
(defn create-search-criteria [{:keys [page sort-by sort-order] :as criteria}]
  (when (s/valid? ::movie-search-criteria criteria)
    (cond-> criteria
      (nil? sort-by) (assoc :sort-by :release-status)
      (nil? sort-order) (assoc :sort-order :desc))))

(defn create-page-result [{:keys [page total-count movies] :as result}]
  (when (and (pos-int? page)
             (nat-int? total-count)
             (vector? movies))
    (assoc result :total-pages 
           (max 1 (int (Math/ceil (/ total-count max-per-page))))))) 