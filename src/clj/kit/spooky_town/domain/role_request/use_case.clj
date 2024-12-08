(ns kit.spooky-town.domain.role-request.use-case
  (:require [kit.spooky-town.domain.role-request.repository.protocol :as repository]
            [kit.spooky-town.domain.user.repository.protocol :as user-repository]
            [kit.spooky-town.domain.user-role.repository.protocol :as user-role-repository]
            [kit.spooky-town.domain.role.repository.protocol :as role-repository]
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

(defrecord RoleRequestUseCaseImpl [with-tx role-request-repository user-repository user-role-repository role-repository event-publisher]
  RoleRequestUseCase
  (request-role-change [_ {:keys [user-uuid role reason]}]
    (with-tx
      (fn [_]
        (f/attempt-all
         [user-id (or (user-repository/find-id-by-uuid user-repository user-uuid)
                     (f/fail :user/not-found))
          user-role (or (first (user-role-repository/find-roles-by-user user-role-repository user-id))
                       (f/fail :user/role-not-found))
          current-role (or (role-repository/find-by-id role-repository (:role-id user-role))
                          (f/fail :role/not-found))
          request (or (entity/create-role-request
                       {:user-id user-id
                        :current-role (:role-name current-role)
                        :requested-role role
                        :reason reason})
                     (f/fail :role-request/invalid-request))
          saved-request (repository/save! role-request-repository request)]
         saved-request))))
  
  (approve-role-request [_ {:keys [admin-uuid request-uuid]}]
    (with-tx
      (fn [_]
        (f/attempt-all
         [admin-id (or (user-repository/find-id-by-uuid user-repository admin-uuid)
                      (f/fail :user/not-found))
          request-id (or (repository/find-id-by-uuid role-request-repository request-uuid)
                        (f/fail :role-request/not-found))
          request (or (repository/find-by-id role-request-repository request-id)
                     (f/fail :role-request/not-found))
          approved-request (or (entity/approve-request request admin-id)
                             (f/fail :role-request/invalid-status))
          updated-request (repository/update-request role-request-repository approved-request)]
         (do
           (event/publish event-publisher
                         :role-request/approved
                         {:user-id (:user-id updated-request)
                          :role (:requested-role updated-request)})
           updated-request)))))
  
  (reject-role-request [_ {:keys [admin-uuid request-uuid reason]}]
    (with-tx
      (fn [_]
        (f/attempt-all
          [admin-id (or (user-repository/find-id-by-uuid user-repository admin-uuid)
                       (f/fail :user/not-found))
           request-id (or (repository/find-id-by-uuid role-request-repository request-uuid)
                         (f/fail :role-request/not-found))
           request (or (repository/find-by-id role-request-repository request-id)
                      (f/fail :role-request/not-found))
           rejected-request (or (entity/reject-request request admin-id reason)
                              (f/fail :role-request/invalid-status))
           updated-request (repository/update-request role-request-repository rejected-request)]
          updated-request))))
  
  (get-request [_ {:keys [request-uuid]}]
    (with-tx
      (fn [_]
        (f/attempt-all
          [request-id (or (repository/find-id-by-uuid role-request-repository request-uuid)
                         (f/fail :role-request/not-found))
           request (or (repository/find-by-id role-request-repository request-id)
                      (f/fail :role-request/not-found))]
          request))))
  
  (get-user-requests [_ {:keys [user-uuid]}]
    (with-tx
      (fn [_]
        (f/attempt-all
         [user-id (or (user-repository/find-id-by-uuid user-repository user-uuid)
                     (f/fail :user/not-found))]
         (repository/find-all-by-user role-request-repository user-id)))))
  
  (get-pending-requests [_]
    (with-tx
      (fn [_]
        (repository/find-all-pending role-request-repository)))))

(defmethod ig/init-key :domain/role-request-use-case
  [_ {:keys [with-tx role-request-repository user-repository user-role-repository role-repository event-publisher]}]
  (->RoleRequestUseCaseImpl with-tx role-request-repository user-repository user-role-repository role-repository event-publisher))