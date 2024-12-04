(ns kit.spooky-town.domain.common.image
  (:require [clojure.spec.alpha :as s]))

;; 이미지 제약조건
(def max-dimension 12000)           ;; 최대 12000px
(def max-area (* 100 1000 1000))   ;; 최대 100 메가픽셀
(def max-size (* 10 1024 1024))    ;; 최대 10MB
(def allowed-types #{"jpeg" "png" "jpg"})

;; 이미지 스펙
(s/def ::file-name string?)
(s/def ::file-type allowed-types)
(s/def ::width (s/and pos-int? #(<= % max-dimension)))
(s/def ::height (s/and pos-int? #(<= % max-dimension)))
(s/def ::size (s/and pos-int? #(<= % max-size)))
(s/def ::area #(<= (* (:width %) (:height %)) max-area))

(s/def ::image
  (s/and (s/keys :req-un [::file-name ::file-type ::width ::height ::size])
         ::area))

(defn create-image [{:keys [file-name file-type width height size] :as image}]
  (when (s/valid? ::image image)
    image)) 