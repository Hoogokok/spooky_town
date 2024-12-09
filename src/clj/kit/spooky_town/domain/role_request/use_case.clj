(ns kit.spooky-town.domain.role-request.use-case
  (:require [kit.spooky-town.domain.role-request.repository.protocol :as role-request-repository]
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
    (with-tx [role-request-repository user-repository user-role-repository role-repository]
      (fn [role-request-repo user-repo user-role-repo role-repo]
        (f/attempt-all
         [user-id (or (user-repository/find-id-by-uuid user-repo user-uuid)
                     (f/fail :user/not-found))
          user-role (or (first (user-role-repository/find-roles-by-user user-role-repo user-id))
                       (f/fail :user/role-not-found))
          current-role (or (role-repository/find-by-id role-repo (:role-id user-role))
                          (f/fail :role/not-found))
          request (or (entity/create-role-request
                       {:user-id user-id
                        :current-role (:role-name current-role)
                        :requested-role role
                        :reason reason})
                     (f/fail :role-request/invalid-request))
          saved-request (role-request-repository/save! role-request-repo request)]
         saved-request))))
  
  (approve-role-request [_ {:keys [admin-uuid request-uuid]}]
    (with-tx [role-request-repository user-repository]
      (fn [role-request-repo user-repo]
        (f/attempt-all
         [admin-id (or (user-repository/find-id-by-uuid user-repo admin-uuid)
                      (f/fail :user/not-found))
          request-id (or (role-request-repository/find-id-by-uuid role-request-repo request-uuid)
                        (f/fail :role-request/not-found))
          request (or (role-request-repository/find-by-id role-request-repo request-id)
                     (f/fail :role-request/not-found))
          approved-request (or (entity/approve-request request admin-id)
                             (f/fail :role-request/invalid-status))
          updated-request (role-request-repository/update-request role-request-repo approved-request)]
         (do
           (event/publish event-publisher
                         :role-request/approved
                         {:user-id (:user-id updated-request)
                          :role (:requested-role updated-request)})
           updated-request)))))
  
  (reject-role-request [_ {:keys [admin-uuid request-uuid reason]}]
    (with-tx [role-request-repository user-repository]
      (fn [role-request-repo user-repo]
        (f/attempt-all
          [admin-id (or (user-repository/find-id-by-uuid user-repo admin-uuid)
                       (f/fail :user/not-found))
           request-id (or (role-request-repository/find-id-by-uuid role-request-repo request-uuid)
                         (f/fail :role-request/not-found))
           request (or (role-request-repository/find-by-id role-request-repo request-id)
                      (f/fail :role-request/not-found))
           rejected-request (or (entity/reject-request request admin-id reason)
                              (f/fail :role-request/invalid-status))
           updated-request (role-request-repository/update-request role-request-repo rejected-request)]
          updated-request))))
  
  (get-request [_ {:keys [request-uuid]}]
    (with-tx [role-request-repository]
      (fn [role-request-repo]
        (f/attempt-all
          [request-id (or (role-request-repository/find-id-by-uuid role-request-repo request-uuid)
                         (f/fail :role-request/not-found))
           request (or (role-request-repository/find-by-id role-request-repo request-id)
                      (f/fail :role-request/not-found))]
          request))))
  
  (get-user-requests [_ {:keys [user-uuid]}]
    (with-tx [user-repository]
      (fn [user-repo]
        (f/attempt-all
         [user-id (or (user-repository/find-id-by-uuid user-repo user-uuid)
                     (f/fail :user/not-found))]
         (role-request-repository/find-all-by-user role-request-repository user-id)))))
  
  (get-pending-requests [_]
    (with-tx [role-request-repository]
      (fn [role-request-repo]
        (role-request-repository/find-all-pending role-request-repo)))))

(defmethod ig/init-key :domain/role-request-use-case
  [_ {:keys [with-tx role-request-repository user-repository user-role-repository role-repository event-publisher]}]
  (->RoleRequestUseCaseImpl with-tx role-request-repository user-repository user-role-repository role-repository event-publisher))