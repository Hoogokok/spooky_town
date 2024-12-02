(ns kit.spooky-town.domain.event.test.subscriber
  (:require [kit.spooky-town.domain.event :as event]))

(defn subscribe [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestEventSubscriber []
  event/EventSubscriber
  (subscribe [this event-type handler] (subscribe this event-type handler))) 