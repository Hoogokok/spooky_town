(ns kit.spooky-town.domain.movie-provider.entity
  (:require [kit.spooky-town.domain.movie-provider.value :as value]
            [clojure.spec.alpha :as s]))

;; 엔티티 스펙
(s/def ::movie-provider
  (s/keys :req-un [::value/movie-provider-id
                   ::value/movie-id
                   ::value/provider-id
                   ::value/created-at
                   ::value/uuid]))

(defrecord MovieProvider [movie-provider-id movie-id provider-id created-at uuid])

(defn create-movie-provider [{:keys [movie-provider-id movie-id provider-id created-at uuid]}]
  (let [movie-provider (map->MovieProvider
                        {:movie-provider-id movie-provider-id
                         :movie-id movie-id
                         :provider-id provider-id
                         :created-at created-at
                         :uuid uuid})]
    (when (s/valid? ::movie-provider movie-provider)
      movie-provider))) 