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