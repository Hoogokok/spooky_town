(ns kit.spooky-town.web.controllers.user
  (:require
   [failjure.core :as f]
   [kit.spooky-town.domain.user.use-case :as use-case]
   [ring.util.http-response :as response]))

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