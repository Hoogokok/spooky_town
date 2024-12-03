(ns kit.spooky-town.web.controllers.user
  (:require
   [failjure.core :as f]
   [kit.spooky-town.domain.user.use-case :as use-case]
   [ring.util.http-response :as response]))

(defn register
  [{:keys [body-params user-use-case]}]
  (let [result (use-case/register-user 
                user-use-case 
                {:email (:email body-params)
                 :name (:name body-params)
                 :password (:password body-params)})]
    (if (f/failed? result)
      (case (f/message result)
        :registration-error/invalid-email
        (response/bad-request {:error "유효하지 않은 이메일입니다"})
        :registration-error/invalid-password
        (response/bad-request {:error "유효하지 않은 비밀번호입니다"})
        :registration-error/invalid-name
        (response/bad-request {:error "유효하지 않은 이름입니다"})
        :registration-error/email-already-exists
        (response/conflict {:error "이미 존재하는 이메일입니다"})
        :registration-error/password-hashing-failed
        (response/internal-server-error {:error "비밀번호 암호화에 실패했습니다"})
        (response/internal-server-error {:error "알 수 없는 오류가 발생했습니다"}))
      (response/created 
       (str "/api/v1/users/" (:user-uuid result))
       {:token (:token result)}))))

(defn withdraw
  [{:keys [body-params user-use-case auth-user]}]
  (let [result (use-case/withdraw 
                user-use-case 
                {:user-uuid (:uuid auth-user)
                 :password (:password body-params)
                 :reason (:reason body-params)})]
    (if (f/failed? result)
      (case (f/message result)
        :withdrawal-error/invalid-password 
        (response/bad-request {:error "비밀번호가 유효하지 않습니다"})
        :withdrawal-error/user-not-found 
        (response/not-found {:error "사용자를 찾을 수 없습니다"})
        :withdrawal-error/already-withdrawn 
        (response/bad-request {:error "이미 탈퇴한 사용자입니다"})
        :withdrawal-error/invalid-credentials 
        (response/unauthorized {:error "비밀번호가 일치하지 않습니다"})
        (response/internal-server-error {:error "알 수 없는 오류가 발생했습니다"}))
      (response/no-content))))

(defn delete-user
  [{:keys [path-params body-params user-use-case auth-user]}]
  (let [result (use-case/delete-user
                user-use-case
                {:admin-uuid (:uuid auth-user)
                 :user-uuid (parse-uuid (:id path-params))
                 :reason (:reason body-params)})]
    (if (f/failed? result)
      (case (f/message result)
        :delete-error/admin-not-found
        (response/not-found {:error "관리자를 찾을 수 없습니다"})
        :delete-error/insufficient-permissions
        (response/forbidden {:error "관리자 권한이 없습니다"})
        :delete-error/user-not-found
        (response/not-found {:error "사용자를 찾을 수 없습니다"})
        :delete-error/already-withdrawn
        (response/bad-request {:error "이미 탈퇴한 사용자입니다"})
        (response/internal-server-error {:error "알 수 없는 오류가 발생했습니다"}))
      (response/no-content))))

(defn authenticate
  [{:keys [body-params user-use-case]}]
  (let [result (use-case/authenticate-user
                user-use-case
                {:email (:email body-params)
                 :password (:password body-params)})]
    (if (f/failed? result)
      (case (f/message result)
        :authentication-error/invalid-email
        (response/bad-request {:error "유효하지 않은 이메일입니다"})
        :authentication-error/invalid-password
        (response/bad-request {:error "유효하지 않은 비밀번호입니다"})
        :authentication-error/user-not-found
        (response/not-found {:error "사용자를 찾을 수 없습니다"})
        :authentication-error/invalid-credentials
        (response/unauthorized {:error "이메일 또는 비밀번호가 일치하지 않습니다"})
        :authentication-error/withdrawn-user
        (response/forbidden {:error "탈퇴한 사용자입니다"})
        (response/internal-server-error {:error "알 수 없는 오류가 발생했습니다"}))
      (response/ok {:token (:token result)}))))

(defn update-profile
  [{:keys [body-params user-use-case auth-user]}]
  (let [result (use-case/update-user
                user-use-case
                {:token (:token auth-user)
                 :name (:name body-params)
                 :email (:email body-params)})]
    (if (f/failed? result)
      (case (f/message result)
        :update-error/invalid-email
        (response/bad-request {:error "유효하지 않은 이메일입니다"})
        :update-error/invalid-name
        (response/bad-request {:error "유효하지 않은 이름입니다"})
        :update-error/email-already-exists
        (response/conflict {:error "이미 존재하는 이메일입니다"})
        :update-error/user-not-found
        (response/not-found {:error "사용자를 찾을 수 없습니다"})
        :update-error/withdrawn-user
        (response/bad-request {:error "탈퇴한 사용자입니다"})
        (response/internal-server-error {:error "알 수 없는 오류가 발생했습니다"}))
      (response/ok result)))) 