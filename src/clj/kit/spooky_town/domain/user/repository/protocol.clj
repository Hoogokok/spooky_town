(ns kit.spooky-town.domain.user.repository.protocol)

(defprotocol UserRepository
  (save! [this user])
  (find-by-id [this id])
  (find-by-email [this email])
  (find-by-uuid [this uuid])
  (delete! [this id])) 
