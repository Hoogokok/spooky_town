(ns kit.spooky-town.infrastructure.auth.email-verification-gateway
  (:require [kit.spooky-town.domain.user.gateway.email-verification :as protocol]
            [integrant.core :as ig]))

(defrecord EmailVerificationGateway [datasource tx-manager queries]
  protocol/EmailVerificationGateway
  
  (save-verification-status! [_ email purpose status]
    (.with-tx tx-manager
      (fn [tx-query-fn]
        (tx-query-fn (:save-email-verification! queries)
                     datasource
                     {:email email
                      :purpose (name purpose)
                      :status (name status)
                      :verified_at (when (= status :verified)
                                   (java.util.Date.))}))))
  
  (get-verification-status [_ email purpose]
    (.with-read-only tx-manager
      (fn [tx-query-fn]
        (when-let [result (tx-query-fn
                           (:get-email-verification-status queries)
                           datasource
                           {:email email
                            :purpose (name purpose)})]
          {:status (keyword (:status result))
           :verified-at (:verified_at result)}))))
  
  (has-verified? [this email purpose]
    (= :verified 
       (:status (protocol/get-verification-status this email purpose)))))

(defmethod ig/init-key :infrastructure/email-verification-gateway
  [_ {:keys [datasource tx-manager queries]}]
  (->EmailVerificationGateway datasource tx-manager queries)) 