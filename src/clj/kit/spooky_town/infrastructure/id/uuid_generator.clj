(ns kit.spooky-town.infrastructure.id.uuid-generator
  (:require [integrant.core :as ig]
            [kit.spooky-town.domain.common.id.protocol :as protocol]))

(defrecord UuidGeneratorImpl []
  protocol/UuidGenerator
  (generate-uuid [_]
    (random-uuid)))

(defmethod ig/init-key :infrastructure/uuid-generator
  [_ _]
  (->UuidGeneratorImpl)) 