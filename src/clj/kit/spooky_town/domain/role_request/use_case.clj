(ns kit.spooky-town.domain.role-request.use-case
  (:require [kit.spooky-town.domain.role-request.entity :as entity]
            [kit.spooky-town.domain.role-request.repository.protocol :as repository]
            [failjure.core :as f]))

(defprotocol RoleRequestUseCase
  (request-role-change [this command])
  (approve-role-request [this command])
  (reject-role-request [this command])
  (get-request [this command])
  (get-user-requests [this command])
  (get-pending-requests [this]))

(defrecord RoleRequestUseCaseImpl [with-tx role-request-repository]
  RoleRequestUseCase
  (request-role-change [_ {:keys [user-id role reason]}]
    (with-tx
      (fn [_]
        (f/attempt-all
         [request (or (entity/create-role-request
                       {:user-id user-id
                        :requested-role role
                        :reason reason})
                      (f/fail :role-request/invalid-request))
          saved-request (repository/save role-request-repository request)]
         saved-request))))
  
  (approve-role-request [_ {:keys [admin-id request-id]}]
    
    (with-tx
      (fn [_]
        (f/attempt-all
         [request (or (repository/find-by-id role-request-repository request-id)
                      (f/fail :role-request/not-found))
          approved-request (or (entity/approve-request request admin-id)
                               (f/fail :role-request/cannot-approve))
          saved-request (repository/update-request role-request-repository approved-request)]
         saved-request))))) 