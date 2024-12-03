(ns kit.spooky-town.domain.user.repository.protocol)

(defprotocol UserRepository
  (save! [this user])
  (find-by-id [this id])
  (find-by-email [this email])
  (find-by-uuid [this uuid] "UUID로 사용자 조회")
  (find-id-by-uuid [this uuid] "UUID로 사용자 ID만 조회")
  (delete! [this id])) 
