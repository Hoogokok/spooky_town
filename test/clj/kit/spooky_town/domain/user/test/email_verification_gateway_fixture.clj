(ns kit.spooky-town.domain.user.test.email-verification-gateway-fixture
  (:require [kit.spooky-town.domain.user.gateway.email-verification :as email-verification-gateway]))

(defn save-verification-status! [_ _ _ _]
  (throw (ex-info "Not implemented" {})))

(defn get-verification-status [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defn has-verified? [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestEmailVerificationGateway []
  email-verification-gateway/EmailVerificationGateway
  (save-verification-status! [this email purpose status] 
    (save-verification-status! this email purpose status))
  
  (get-verification-status [this email purpose]
    (get-verification-status this email purpose))
  
  (has-verified? [this email purpose]
    (has-verified? this email purpose))) 