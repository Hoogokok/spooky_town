(ns kit.spooky-town.web.controllers.role-request
  (:require [kit.spooky-town.domain.role-request.use-case :as use-case]
            [ring.util.http-response :as response]
            [failjure.core :as f]))

(defn create-request
  "역할 변경 요청을 생성합니다."
  [{:keys [body-params role-request-use-case]}]
  (let [{:keys [role reason]} body-params
        user-id (get-in body-params [:identity :user-id])]
    (if (and role reason)
      (let [result (use-case/request-role-change 
                    role-request-use-case 
                    {:user-id user-id 
                     :role role 
                     :reason reason})]
        (if (f/ok? result)
          (response/created "" {:message "Role request created successfully."})
          (response/bad-request {:error (f/message result)})))
      (response/bad-request {:error "Invalid request parameters."}))))

(defn get-pending-requests
  "대기 중인 역할 변경 요청 목록을 조회합니다."
  [{:keys [role-request-use-case identity]}]
  (if (contains? (:roles identity) :admin)
    (let [requests (use-case/get-pending-requests role-request-use-case)]
      (response/ok requests))
    (response/forbidden {:error "Unauthorized access. Admin role required."}))) 