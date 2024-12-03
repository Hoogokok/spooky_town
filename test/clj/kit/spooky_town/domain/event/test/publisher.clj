(ns kit.spooky-town.domain.event.test.publisher
  (:require [kit.spooky-town.domain.event :as event]))

(defn publish [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestEventPublisher [published-events]
  event/EventPublisher
  (publish [this event-type payload] (publish this event-type payload)))