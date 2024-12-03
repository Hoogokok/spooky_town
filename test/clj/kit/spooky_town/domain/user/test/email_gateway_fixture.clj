(ns kit.spooky-town.domain.user.test.email-gateway-fixture
  (:require [kit.spooky-town.domain.user.gateway.email :as email-gateway]))

(defn send-verification-email [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defn send-password-reset-email [_ _ _]
  (throw (ex-info "Not implemented" {})))


(defn send-email-change-verification [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestEmailGateway []
  email-gateway/EmailGateway
  (send-verification-email [this email purpose] (send-verification-email this email purpose))
  (send-password-reset-email [this email purpose] (send-password-reset-email this email purpose))
  (send-email-change-verification [this email purpose] (send-email-change-verification this email purpose)))