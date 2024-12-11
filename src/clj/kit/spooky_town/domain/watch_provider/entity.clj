(ns kit.spooky-town.domain.watch-provider.entity
  (:require [kit.spooky-town.domain.watch-provider.value :as value]
            [clojure.spec.alpha :as s]))

;; 엔티티 스펙
(s/def ::watch-provider
  (s/keys :req-un [::value/provider-id
                   ::value/provider-name
                   ::value/created-at]
          :opt-un [::value/logo-url]))

(defrecord WatchProvider [provider-id provider-name logo-url created-at]
  Object
  (toString [this]
    (str "WatchProvider[" provider-id "," provider-name "]")))

(defn create-watch-provider [{:keys [provider-id provider-name logo-url created-at]}]
  (let [watch-provider (map->WatchProvider
                        {:provider-id provider-id
                         :provider-name provider-name
                         :logo-url logo-url
                         :created-at created-at})]
    (when (s/valid? ::watch-provider watch-provider)
      watch-provider)))