(ns kit.spooky-town.domain.common.id.test.uuid-generator
  (:require [kit.spooky-town.domain.common.id.protocol :as protocol]))

(defrecord TestUuidGenerator []
  protocol/UuidGenerator
  (generate-uuid [_]
    #uuid "550e8400-e29b-41d4-a716-446655440000")) 