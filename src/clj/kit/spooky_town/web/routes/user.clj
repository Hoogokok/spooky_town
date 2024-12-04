(ns kit.spooky-town.web.routes.user
  (:require [kit.spooky-town.web.controllers.user :as user]
            [kit.spooky-town.web.controllers.password :as password]))

(defn user-routes [{:keys [user-use-case]}]
  ["/users"])  ;; 사용자 관리 기능을 여기에 추가할 예정
 