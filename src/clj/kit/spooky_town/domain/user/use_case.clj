(ns kit.spooky-town.domain.user.use-case
  (:require
   [failjure.core :as f]
   [integrant.core :as ig]
   [kit.spooky-town.domain.event :as event]
   [kit.spooky-town.domain.user.entity :as entity :refer [admin?]]
   [kit.spooky-town.domain.user.gateway.email :as email-gateway]
   [kit.spooky-town.domain.user.gateway.email-token :as email-token-gateway]
   [kit.spooky-town.domain.user.gateway.password :as password-gateway]
   [kit.spooky-town.domain.user.gateway.token :as token-gateway]
   [kit.spooky-town.domain.user.gateway.email-verification :as email-verification-gateway]
   [kit.spooky-town.domain.user.repository.protocol :refer [find-by-email
                                                            find-by-id
                                                            find-by-uuid
                                                            find-id-by-uuid
                                                            save!]]
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
    "이벤트 구독을 초기화합니다.")
  (request-password-reset [this command]
    "비밀번호 초기화를 요청합니다.")
  (reset-password [this command]
    "비밀번호를 초기화합니다.")
  (withdraw [this command]
    "사용자가 회원 탈퇴를 진행합니다.")
  (delete-user [this command]
    "관리자가 사용자를 탈퇴 처리합니다.")
  (request-email-verification [this email purpose]
    "이메일 인증을 요청합니다. 
     purpose는 :registration, :password-reset, :email-change 중 하나입니다.")
  (verify-email [this token]
    "토큰을 검증하고 이메일 인증을 완료합니다.
     성공 시 이메일과 용도를 반환합니다.")
  (check-email-verification-status [this email]
    "해당 이메일의 인증 상태를 확인합니다."))

(def token-expires-in-sec (* 60 60 24))

(defrecord RegisterUserCommand [email name password])

(defrecord AuthenticateUserCommand [email password])

(defrecord UpdateUserCommand [token name email password])

(defrecord WithdrawCommand [user-uuid password reason])

(defrecord DeleteUserCommand [admin-uuid user-uuid reason])

(defrecord UserUseCaseImpl [with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway]
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
      _ (when (:deleted-at user)
          (f/fail :authentication-error/withdrawn-user))
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
      user (or (find-by-id user-repository user-id)
               (f/fail :update-error/user-not-found))
      _ (when (:deleted-at user)
          (f/fail :update-error/withdrawn-user))
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
                 [_ (when (and email' (find-by-email repo email'))
                     (f/fail :update-error/email-already-exists))
                  updated-user (as-> user user'
                               (if name' (entity/update-name user' name') user')
                               (if email' (entity/update-email user' email') user')
                               (if hashed-password (entity/update-password user' hashed-password) user'))
                  _ (save! repo updated-user)]
                 updated-user)))]
     {:user-uuid (:uuid result)
      :email (:email result)
      :name (:name result)}))

  (update-user-role [_ {:keys [admin-uuid user-uuid role]}]
    (with-tx user-repository
      (fn [repo]
        (f/attempt-all
         [admin-id (or (find-id-by-uuid repo admin-uuid)
                      (f/fail :admin/not-found))
          user-id (or (find-id-by-uuid repo user-uuid)
                      (f/fail :user/not-found))
          [admin user] [(find-by-id repo admin-id)
                       (find-by-id repo user-id)]
          _ (when (or (nil? admin) (nil? user))
              (f/fail :user/not-found))
          _ (when-not (admin? admin)
              (f/fail :update-error/insufficient-permissions))
          _ (when (:deleted-at user)
              (f/fail :update-error/withdrawn-user))
          updated-user (assoc user :roles #{role})
          saved-user (save! repo updated-user)]
         {:user-uuid (:uuid saved-user)
          :roles (:roles saved-user)}))))

  (request-password-reset [_ {:keys [email]}]
    (with-tx user-repository
      (fn [repo]
        (f/attempt-all
         [email' (or (value/create-email email)
                    (f/fail :password-reset/invalid-email))
          user (or (find-by-email repo email')
                  (f/fail :password-reset/user-not-found))
          _ (when (:deleted-at user)
              (f/fail :password-reset/withdrawn-user))
          _ (when-let [existing-token (token-gateway/find-valid-token token-gateway (:uuid user))]
              (f/fail :password-reset/token-already-exists))
          _ (when (token-gateway/check-rate-limit token-gateway email' :password-reset)
              (f/fail :password-reset/rate-limit-exceeded))
          token-ttl (java.time.Duration/ofHours 24)
          reset-token (token-gateway/generate token-gateway (:uuid user) token-ttl)]
         {:token reset-token}))))

  (reset-password [_ {:keys [token new-password]}]
    (with-tx user-repository
      (fn [repo]
        (f/attempt-all
         [user-uuid (token-gateway/verify token-gateway token)
          user (or (find-by-uuid repo user-uuid)
                  (f/fail :password-reset/user-not-found))
          _ (when (:deleted-at user)
              (f/fail :password-reset/withdrawn-user))
          hashed-password (password-gateway/hash-password password-gateway new-password)
          updated-user (assoc user :password hashed-password)
          saved-user (save! repo updated-user)]
         {:user-uuid (:uuid saved-user)}))))

  (init [this]
    (event/subscribe event-subscriber
               :role-request/approved
               (fn [{:keys [user-id role]}]
                 (update-user-role this {:user-id user-id :role role}))))

  (withdraw [_ {:keys [user-uuid password reason]}]
    (f/attempt-all
     [password' (or (value/create-password password)
                   (f/fail :withdrawal-error/invalid-password))
      result (with-tx user-repository
               (fn [repo]
                 (f/attempt-all
                  [user (or (find-by-uuid repo user-uuid)
                           (f/fail :withdrawal-error/user-not-found))
                   _ (when (:deleted-at user)
                       (f/fail :withdrawal-error/already-withdrawn))
                   _ (or (password-gateway/verify-password password-gateway
                                                         password'
                                                         (:hashed-password user))
                         (f/fail :withdrawal-error/invalid-credentials))
                   withdrawn-user (entity/mark-as-withdrawn user reason)
                   _ (save! repo withdrawn-user)]
                  true)))]
     {:success true}))

  (delete-user [_ {:keys [admin-uuid user-uuid reason]}]
    (f/attempt-all
     [admin (or (find-by-id user-repository admin-uuid)
                (f/fail :delete-error/admin-not-found))
      _ (when-not (admin? admin)
          (f/fail :delete-error/insufficient-permissions))
      result (with-tx user-repository
               (fn [repo]
                 (f/attempt-all
                  [user (or (find-by-id repo user-uuid)
                            (f/fail :delete-error/user-not-found))
                   _ (when (:deleted-at user)
                       (f/fail :delete-error/already-withdrawn))
                   withdrawn-user (entity/mark-as-withdrawn user reason)
                   _ (save! repo withdrawn-user)]
                  true)))]
     {:success true}))

  (request-email-verification [_ email purpose]
    (if-not (#{:registration :password-reset :email-change} purpose)
      (f/fail :email-verification/invalid-purpose)
      (f/attempt-all
        [user (or (with-tx user-repository
                    (fn [repo]
                      (find-by-email repo email)))
                  (f/fail :email-verification/user-not-found))
         token (email-token-gateway/generate-token email-token-gateway email purpose)
         _ (case purpose
             :registration (email-gateway/send-verification-email email-gateway email token)
             :password-reset (email-gateway/send-password-reset-email email-gateway email token)
             :email-change (email-gateway/send-email-change-verification email-gateway email token))]
        {:token token}
        (f/when-failed [e]
          e))))

  (verify-email [_ token]
    (if (empty? token)
      (f/fail :email-verification/invalid-token)
      (f/attempt-all
        [{:keys [email purpose] :as result} (email-token-gateway/verify-token 
                                            email-token-gateway token)
         _ (email-verification-gateway/save-verification-status! 
             email-verification-gateway email purpose :verified)]
        result
        (f/when-failed [e]
          (f/fail :email-verification/invalid-token)))))

  (check-email-verification-status [_ email]
    (if (empty? email)
      (f/fail :email-verification/invalid-email)
      (f/attempt-all
        [status (email-verification-gateway/get-verification-status 
                 email-verification-gateway email :registration)]
        (if status
          {:email email :status status}
          (f/fail :email-verification/not-verified))))))

(defmethod ig/init-key :domain/user-use-case
  [_ {:keys [with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway]}]
  (->UserUseCaseImpl with-tx password-gateway token-gateway user-repository event-subscriber email-gateway email-token-gateway email-verification-gateway))