(ns kit.spooky-town.domain.movie-provider.value
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

;; movie-provider-id
(s/def ::movie-provider-id (s/and string? #(not (str/blank? %))))

(defn create-movie-provider-id [value]
  (when (s/valid? ::movie-provider-id value)
    value))

;; movie-id
(s/def ::movie-id (s/and string? #(not (str/blank? %))))

(defn create-movie-id [value]
  (when (s/valid? ::movie-id value)
    value))

;; provider-id
(s/def ::provider-id (s/and string? #(not (str/blank? %))))

(defn create-provider-id [value]
  (when (s/valid? ::provider-id value)
    value))

;; created-at
(s/def ::created-at inst?)

(defn create-timestamp [value]
  (when (s/valid? ::created-at value)
    value))

;; uuid
(s/def ::uuid uuid?) 