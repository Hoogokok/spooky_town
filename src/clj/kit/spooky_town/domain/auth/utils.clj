(ns kit.spooky-town.domain.auth.utils
  (:import [java.time Instant]))

(defn now []
  (Instant/now))

(defn expired? [expires-at]
  (.isAfter (now) expires-at)) 