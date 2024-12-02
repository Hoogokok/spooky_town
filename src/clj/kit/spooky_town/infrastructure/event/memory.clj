(ns kit.spooky-town.infrastructure.event.memory
  (:require [kit.spooky-town.domain.event :as event]
            [integrant.core :as ig]))

(defrecord MemoryEventBus [handlers]
  event/EventPublisher
  (publish [_ event-type payload]
    (when-let [handlers (get @handlers event-type)]
      (doseq [handler handlers]
        (handler payload))))

  event/EventSubscriber
  (subscribe [_ event-type handler]
    (swap! handlers update event-type (fnil conj #{}) handler)))

(defmethod ig/init-key :infrastructure/event-bus [_ _]
  (->MemoryEventBus (atom {}))) 