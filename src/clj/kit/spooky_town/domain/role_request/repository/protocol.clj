(ns kit.spooky-town.domain.role-request.repository.protocol)

(defprotocol RoleRequestRepository
  (save [this role-request])
  (find-by-id [this id])
  (find-by-uuid [this uuid])
  (find-id-by-uuid [this uuid])
  (find-all-by-user [this user-id])
  (find-all-pending [this])
  (update-request [this role-request])) 