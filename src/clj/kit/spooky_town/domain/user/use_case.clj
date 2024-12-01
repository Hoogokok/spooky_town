(ns kit.spooky-town.domain.user.use-case
  (:require
   [failjure.core :as f]
   [kit.spooky-town.domain.user.entity :as entity]
   [kit.spooky-town.domain.user.gateway.password :as password-gateway]
   [kit.spooky-town.domain.user.gateway.token :as token-gateway]
   [kit.spooky-town.domain.user.repository.user-repository :refer [find-by-email
                                                                   find-by-uuid
                                                                   save!]]
   [kit.spooky-town.domain.user.value :as value]))

(defprotocol UserUseCase
  (register-user [this command]
    "새로운 사용자를 등록합니다.")
  (authenticate-user [this command]
    "사용자 인증을 수행합니다.")
  (update-user-name [this user-uuid command]
    "사용자 이름을 업데이트합니다."))

(def token-expires-in-sec (* 60 60 24))

(defrecord RegisterUserCommand [email name password])

(defrecord AuthenticateUserCommand [email password])

(defrecord UpdateUserNameCommand [name])

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
      :token token}))

  (update-user-name [_ user-uuid {:keys [name]}]
    (f/attempt-all
     [name' (or (value/create-name name)
                (f/fail :update-error/invalid-name))
      _ (with-tx user-repository
          (fn [repo]
            (f/attempt-all
             [user (or (find-by-uuid repo user-uuid)
                      (f/fail :update-error/user-not-found))
              updated-user (entity/update-name user name')
              _ (save! repo updated-user)]
             true)))]
     {:user-uuid user-uuid})))
