(ns kit.spooky-town.domain.watch-provider.value
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

;; provider-id
(s/def ::provider-id (s/and string? #(not (str/blank? %))))

(defrecord WatchProviderId [value])

(defn create-provider-id [value]
  (when (s/valid? ::provider-id value)
    (->WatchProviderId value)))

;; provider-name
(def max-provider-name-length 50)
(s/def ::provider-name (s/and string?
                              #(not (str/blank? %))
                              #(<= (count %) max-provider-name-length)))

(defn create-provider-name [value]
  (when (s/valid? ::provider-name value)
    value))

;; logo-url
(def url-pattern #"^https?://.*")
(s/def ::logo-url (s/nilable (s/and string?
                                    #(re-matches url-pattern %))))

(defn create-logo-url [value]
  (when (or (nil? value)
            (s/valid? ::logo-url value))
    value))

;; created-at
(s/def ::created-at inst?)


(defn create-timestamp [value]
  (when (s/valid? ::created-at value)
    value))

;; uuid
(s/def ::uuid uuid?)