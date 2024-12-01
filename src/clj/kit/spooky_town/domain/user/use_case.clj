(ns kit.spooky-town.domain.user.use-case
  (:require [kit.spooky-town.domain.user.entity :as entity]
            [kit.spooky-town.domain.user.value :as value]
            [kit.spooky-town.domain.user.gateway.token :as token-gateway]
            [kit.spooky-town.domain.user.repository.user-repository :refer [find-by-email save!]]
            [kit.spooky-town.domain.user.gateway.password :as password-gateway]
            [failjure.core :as f]))

(defprotocol UserUseCase
  (register-user [this command]
    "새로운 사용자를 등록합니다.")
  (authenticate-user [this command]
    "사용자 인증을 수행합니다."))

(def token-expires-in-sec (* 60 60 24))

(defrecord RegisterUserCommand [email name password])

(defrecord AuthenticateUserCommand [email password])

(defrecord UserUseCaseImpl [with-tx password-gateway token-gateway user-repository]
  UserUseCase
  (register-user [_ {:keys [email name password]}]
    (f/attempt-all
     [uuid (random-uuid)
      created-at (java.util.Date.)
      email' (or (value/create-email email)
                 (f/fail :registration-error/invalid-email))
      name' (or (value/create-name name)
                (f/fail :registration-error/invalid-name))
      password' (or (value/create-password password)
                    (f/fail :registration-error/invalid-password))
      hashed-password (or (password-gateway/hash-password password-gateway password')
                          (f/fail :registration-error/password-hashing-failed))
      _ (with-tx
          user-repository                ;; 첫 번째 인자: repository
          (fn [repo]                     ;; 두 번째 인자: 실행할 함수
            (f/attempt-all
             [_ (when-not (nil? (find-by-email repo email'))
                  (f/fail :registration-error/email-already-exists))
              user (entity/create-user
                    {:uuid uuid
                     :email email'
                     :name name'
                     :hashed-password hashed-password
                     :created-at created-at})
              _ (save! repo user)]
             true)))
      token-ttl (java.time.Duration/ofSeconds token-expires-in-sec)
      token (token-gateway/generate token-gateway uuid token-ttl)]
     {:user-uuid uuid
      :token token}))

  (authenticate-user [_ {:keys [email password]}]
    (f/attempt-all
     [email' (or (value/create-email email)
                 (f/fail :authentication-error/invalid-email))
      password' (or (value/create-password password)
                    (f/fail :authentication-error/invalid-password))
      user (or (with-tx user-repository
                 (fn [repo]
                   (find-by-email repo email')))
               (f/fail :authentication-error/user-not-found))
      _ (or (password-gateway/verify-password password-gateway
                                              password'
                                              (:hashed-password user))
            (f/fail :authentication-error/invalid-credentials))
      token-ttl (java.time.Duration/ofSeconds token-expires-in-sec)
      token (token-gateway/generate token-gateway (:uuid user) token-ttl)]
     {:user-uuid (:uuid user)
      :token token})))
