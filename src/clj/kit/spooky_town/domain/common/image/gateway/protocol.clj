(ns kit.spooky-town.domain.common.image.gateway.protocol
  (:require [kit.spooky-town.domain.common.image :as image]))

(defprotocol ImageUploadGateway
  (upload [this upload-file]
    "이미지를 업로드하고 결과를 반환합니다.
     upload-file: image/upload-file 스펙을 만족하는 맵
     returns: {:url string? :width (nullable int) :height (nullable int)}")) 