(ns kit.spooky-town.domain.role-request.use-case
  (:require [kit.spooky-town.domain.role-request.repository.protocol :as repository]
            [kit.spooky-town.domain.role-request.entity :as entity]
            [kit.spooky-town.domain.event :as event]
            [failjure.core :as f]
            [integrant.core :as ig]))

(defprotocol RoleRequestUseCase
  (request-role-change [this command])
  (approve-role-request [this command])
  (reject-role-request [this command])
  (get-request [this command])
  (get-user-requests [this command])
  (get-pending-requests [this]))

(defrecord RoleRequestUseCaseImpl [with-tx role-request-repository event-publisher]
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
           saved-request (repository/update-request role-request-repository approved-request)
           _ (event/publish event-publisher 
                          :role-request/approved 
                          {:user-id (:user-id saved-request)
                           :role (:requested-role saved-request)})]
          saved-request))))
  
  (reject-role-request [_ {:keys [admin-id request-id reason]}]
    (with-tx
      (fn [_]
        (f/attempt-all
          [request (or (repository/find-by-id role-request-repository request-id)
                      (f/fail :role-request/not-found))
           rejected-request (or (entity/reject-request request admin-id reason)
                              (f/fail :role-request/cannot-reject))
           saved-request (repository/update-request role-request-repository rejected-request)]
          saved-request))))
  
  (get-request [_ {:keys [request-id]}]
    (with-tx
      (fn [_]
        (f/attempt-all
          [request (or (repository/find-by-id role-request-repository request-id)
                      (f/fail :role-request/not-found))]
          request))))
  
  (get-user-requests [_ {:keys [user-id]}]
    (with-tx
      (fn [_]
        (repository/find-all-by-user role-request-repository user-id))))
  
  (get-pending-requests [_]
    (with-tx
      (fn [_]
        (repository/find-all-pending role-request-repository)))))

(defmethod ig/init-key :domain/role-request-use-case
  [_ {:keys [with-tx role-request-repository event-publisher]}]
  (->RoleRequestUseCaseImpl with-tx role-request-repository event-publisher))