(ns kit.spooky-town.domain.common.image.test.gateway
  (:require [kit.spooky-town.domain.common.image.gateway.protocol :refer [ImageUploadGateway]]))

(defn upload [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestImageUploadGateway []
  ImageUploadGateway
  (upload [this file] (upload this file))) 