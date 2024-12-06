(ns kit.spooky-town.infrastructure.id.ulid
  (:require
 [kit.spooky-town.domain.common.id.protocol :as protocol]
   [integrant.core :as ig])
 (:import
  [com.github.f4b6a3.ulid UlidCreator]))

(defrecord UlidGenerator []
  protocol/IdGenerator
  (generate-ulid [_]
    (.toString (UlidCreator/getUlid))))

(defmethod ig/init-key :infrastructure/ulid-generator [_ _]
  (->UlidGenerator)) 