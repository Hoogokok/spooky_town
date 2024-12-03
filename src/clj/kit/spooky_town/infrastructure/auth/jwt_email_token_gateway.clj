(ns kit.spooky-town.infrastructure.auth.jwt-email-token-gateway
  (:require [kit.spooky-town.domain.user.gateway.email-token :as email-token-gateway]
            [buddy.sign.jwt :as jwt]
            [failjure.core :as f]
            [integrant.core :as ig]))

(def expiration-times
  {:registration    (* 24 60 60)  ; 24시간
   :password-reset  (* 1 60 60)   ; 1시간
   :email-change    (* 1 60 60)}) ; 1시간

(defrecord JWTEmailTokenGateway [secret]
  email-token-gateway/EmailTokenGateway
  (generate-token [_ email purpose]
    (let [exp (get expiration-times purpose (* 24 60 60))
          claims {:email email
                 :purpose purpose
                 :exp (+ (quot (System/currentTimeMillis) 1000) exp)}]
      (jwt/sign claims secret)))
  
  (verify-token [_ token]
    (try
      (let [claims (jwt/unsign token secret)]
        {:email (:email claims)
         :purpose (keyword (:purpose claims))})
      (catch Exception _
        (f/fail :invalid-token)))))

(defmethod ig/init-key :infrastructure/email-token-gateway
  [_ {:keys [secret]}]
  (->JWTEmailTokenGateway secret)) 