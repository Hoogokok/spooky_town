(ns kit.spooky-town.domain.user.use-case
  (:require
   [failjure.core :as f]
   [integrant.core :as ig]
   [kit.spooky-town.domain.event :as event]
   [kit.spooky-town.domain.user.entity :as entity]
   [kit.spooky-town.domain.user.gateway.password :as password-gateway]
   [kit.spooky-town.domain.user.gateway.token :as token-gateway]
   [kit.spooky-town.domain.user.repository.protocol :refer [find-by-email
                                                            find-by-id save!]]
   [kit.spooky-town.domain.user.value :as value]))

(defprotocol UserUseCase
  (register-user [this command]
    "새로운 사용자를 등록합니다.")
  (authenticate-user [this command]
    "사용자 인증을 수행합니다.")
  (update-user [this command]
    "사용자 정보를 업데이트합니다.")
  (update-user-role [this command]
    "사용자의 역할을 업데이트합니다.")
  (init [this]
    "이벤트 구독을 초기화합니다."))

(def token-expires-in-sec (* 60 60 24))

(defrecord RegisterUserCommand [email name password])

(defrecord AuthenticateUserCommand [email password])

(defrecord UpdateUserCommand [token name email password])

(defrecord UserUseCaseImpl [with-tx password-gateway token-gateway user-repository event-subscriber]
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

  (update-user [_ {:keys [token name email password]}]
    (f/attempt-all
     [user-id (or (token-gateway/verify token-gateway token)
                  (f/fail :update-error/invalid-token))
      name' (when name 
             (or (value/create-name name)
                 (f/fail :update-error/invalid-name)))
      email' (when email
              (or (value/create-email email)
                  (f/fail :update-error/invalid-email)))
      password' (when password
                 (or (value/create-password password)
                     (f/fail :update-error/invalid-password)))
      hashed-password (when password'
                       (or (password-gateway/hash-password password-gateway password')
                           (f/fail :update-error/password-hashing-failed)))
      result (with-tx user-repository
              (fn [repo]
                (f/attempt-all
                 [user (or (find-by-id repo user-id)
                          (f/fail :update-error/user-not-found))
                  _ (when (and email' (find-by-email repo email'))
                      (f/fail :update-error/email-already-exists))
                  updated-user (as-> user user'
                               (if name' (entity/update-name user' name') user')
                               (if email' (entity/update-email user' email') user')
                               (if hashed-password (entity/update-password user' hashed-password) user'))
                  _ (save! repo updated-user)]
                 updated-user)))]
     {:user-uuid (:uuid result)}))

  (update-user-role [_ {:keys [user-id role]}]
    (with-tx user-repository
      (fn [_]
        (f/attempt-all
          [user (or (find-by-id user-repository user-id)
                   (f/fail :user/not-found))
           updated-user (assoc user :roles #{role})
           saved-user (save! user-repository updated-user)]
          saved-user))))

  ;; 이벤트 구독 설정을 위한 초기화 함수
  (init [this]
    (event/subscribe event-subscriber
                    :role-request/approved
                    (fn [{:keys [user-id role]}]
                      (update-user-role this {:user-id user-id :role role})))))


(defmethod ig/init-key :domain/user-use-case
  [_ {:keys [with-tx password-gateway token-gateway user-repository event-subscriber]}]
  (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber))