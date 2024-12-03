(ns kit.spooky-town.web.controllers.password
  (:require [failjure.core :as f]
            [kit.spooky-town.domain.user.use-case :as use-case]
            [ring.util.http-response :as response]))

(defn request-reset
  [{:keys [body-params user-use-case]}]
  (let [result (use-case/request-password-reset 
                user-use-case 
                {:email (:email body-params)})]
    (if (f/failed? result)
      (case (f/message result)
        :password-reset/invalid-email
        (response/bad-request {:error "유효하지 않은 이메일입니다"})
        :password-reset/user-not-found
        (response/not-found {:error "사용자를 찾을 수 없습니다"})
        :password-reset/withdrawn-user
        (response/bad-request {:error "탈퇴한 사용자입니다"})
        :password-reset/token-already-exists
        (response/bad-request {:error "이미 유효한 토큰이 존재합니다"})
        :password-reset/rate-limit-exceeded
        (response/too-many-requests {:error "요청 횟수가 초과되었습니다"})
        (response/internal-server-error {:error "알 수 없는 오류가 발생했습니다"}))
      (response/ok result))))

(defn reset-password
  [{:keys [body-params user-use-case]}]
  (let [{:keys [token new-password]} body-params
        result (use-case/reset-password 
                user-use-case 
                {:token token
                 :new-password new-password})]
    (if (f/failed? result)
      (response/bad-request {:error (f/message result)})
      (response/ok result)))) 