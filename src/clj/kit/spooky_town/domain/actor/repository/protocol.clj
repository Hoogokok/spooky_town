(ns kit.spooky-town.domain.actor.repository.protocol)

(defprotocol ActorRepository
  (find-by-id [this id] "ID로 배우를 조회합니다.")
  (find-by-ids [this ids] "여러 ID로 배우들을 조회합니다.")
  (save! [this actor] "새로운 배우를 저장합니다.")) 