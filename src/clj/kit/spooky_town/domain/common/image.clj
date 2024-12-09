(ns kit.spooky-town.domain.common.image
  (:require [clojure.spec.alpha :as s]))

;; 파일 제약조건
(def max-file-size (* 10 1024 1024))    ;; 최대 10MB
(def allowed-types #{"jpeg" "png" "jpg"})

;; 이미지 차원 제약조건
(def max-dimension 12000)           ;; 최대 12000px
(def max-area (* 100 1000 1000))   ;; 최대 100 메가픽셀

;; 업로드할 파일 스펙
(s/def ::file-name string?)
(s/def ::file-type allowed-types)
(s/def ::file-size (s/and pos-int? #(<= % max-file-size)))
(s/def ::width (s/nilable (s/and pos-int? #(<= % max-dimension))))
(s/def ::height (s/nilable (s/and pos-int? #(<= % max-dimension))))
(s/def ::area #(if (and (:width %) (:height %))
                 (<= (* (:width %) (:height %)) max-area)
                 true))

(s/def ::upload-file
  (s/and (s/keys :req-un [::file-name ::file-type ::file-size]
                 :opt-un [::width ::height])
         ::area))

;; 이미지 URL 제약조건
(def url-pattern #"^https?://.*")
(s/def ::url (s/and string? 
                    not-empty 
                    #(re-matches url-pattern %)))

;; 업로드된 이미지 스펙
(s/def ::image
  (s/and (s/keys :req-un [::url]
                 :opt-un [::width ::height])
         ::area))

(defn create-upload-file [{:keys [file-name file-type file-size] :as file}]
  (when (s/valid? ::upload-file file)
    file))

(defn create-image [{:keys [url width height] :as image}]
  (when (and (s/valid? ::image image)
             (re-matches url-pattern url))
    image)) 