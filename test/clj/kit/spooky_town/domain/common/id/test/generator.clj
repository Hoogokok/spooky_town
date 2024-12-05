(ns kit.spooky-town.domain.common.id.test.generator
  (:require [kit.spooky-town.domain.common.id.protocol :refer [IdGenerator]]))

(defn generate-ulid [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestIdGenerator []
  IdGenerator
  (generate-ulid [this] (generate-ulid this nil))) 