(ns kit.spooky-town.application.auth.service
  (:require [kit.spooky-town.domain.auth.core :as auth]
            [integrant.core :as ig]))

(defprotocol CredentialsValidator
  (valid-credentials? [this credentials]
   "주어진 자격 증명(credentials)의 유효성을 검사합니다."))

(defprotocol UserAuthService
  (authenticate-user [this email password]
    "사용자 인증을 수행하고 AuthToken을 반환합니다.")
  (get-authenticated-user [this token]
    "토큰으로부터 인증된 사용자 정보를 조회합니다."))

(defrecord AuthService [auth-provider db-conn]
  UserAuthService
  (authenticate-user [this email password]
    (let [credentials (auth/->Credentials email password)]
      (auth/authenticate auth-provider credentials)))
  
  (get-authenticated-user [this token]
    (when-let [claims (auth/verify-token auth-provider token)]
      ;; TODO: DB에서 사용자 정보 조회 구현
      {:email (:email claims)})))

(defmethod ig/init-key :auth/service [_ {:keys [auth-provider db-conn]}]
  (->AuthService auth-provider db-conn))

;; DB 연동 구현체는 별도로 작성 