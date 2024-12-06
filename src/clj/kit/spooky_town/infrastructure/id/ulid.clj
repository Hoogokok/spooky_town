(ns kit.spooky-town.infrastructure.id.ulid
  (:require [com.github.f4b6a3/ulid-creator :as ulid]
            [kit.spooky-town.domain.common.id.protocol :as protocol]
            [integrant.core :as ig]))

(defrecord UlidGenerator []
  protocol/IdGenerator
  (generate-ulid [_]
    (ulid/ulid)))

(defmethod ig/init-key ::ulid-generator [_ _]
  (->UlidGenerator)) 