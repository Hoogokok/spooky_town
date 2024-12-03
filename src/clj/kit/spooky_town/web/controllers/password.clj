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
      (response/bad-request {:error (f/message result)})
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