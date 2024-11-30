(ns kit.spooky-town.application.auth.service
  (:require [kit.spooky-town.domain.auth.core :as auth]))

(defprotocol CredentialsValidator
  (valid-credentials? [this credentials]))

;; DB 연동 구현체는 별도로 작성 