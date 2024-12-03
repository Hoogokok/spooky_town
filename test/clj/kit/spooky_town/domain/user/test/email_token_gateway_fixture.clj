(ns kit.spooky-town.domain.user.test.email-token-gateway-fixture
  (:require [kit.spooky-town.domain.user.gateway.email-token :as email-token-gateway]))

(defn generate-token [_ _ _]
  (throw (ex-info "Not implemented" {})))

(defn verify-token [_ _]
  (throw (ex-info "Not implemented" {})))

(defrecord TestEmailTokenGateway []
  email-token-gateway/EmailTokenGateway
  (generate-token [this email purpose] (generate-token this email purpose))
  (verify-token [this token] (verify-token this token)))