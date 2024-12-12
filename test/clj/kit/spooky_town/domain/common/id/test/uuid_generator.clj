(ns kit.spooky-town.domain.common.id.test.uuid-generator
  (:require [kit.spooky-town.domain.common.id.protocol :as protocol]))

(defn generate-uuid [_]
  (throw (ex-info "Not implemented" {})))

(defrecord TestUuidGenerator []
  protocol/UuidGenerator
  (generate-uuid [this] (generate-uuid this))) 